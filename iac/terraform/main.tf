# Generate SSH Key Pair
resource "tls_private_key" "ssh" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

# Save private key locally
resource "local_file" "private_key" {
  content         = tls_private_key.ssh.private_key_pem
  filename        = "${path.module}/keys/id_rsa"
  file_permission = "0600"
}

# Save public key locally
resource "local_file" "public_key" {
  content         = tls_private_key.ssh.public_key_openssh
  filename        = "${path.module}/keys/id_rsa.pub"
  file_permission = "0644"
}

# Resource Group
resource "azurerm_resource_group" "main" {
  name     = "${var.resource_prefix}-rg"
  location = var.location

  tags = {
    environment = "testing"
    project     = "todo-app"
  }
}

# Virtual Network
resource "azurerm_virtual_network" "main" {
  name                = "${var.resource_prefix}-vnet"
  address_space       = ["10.0.0.0/16"]
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  tags = {
    environment = "testing"
    project     = "todo-app"
  }
}

# Subnet
resource "azurerm_subnet" "main" {
  name                 = "${var.resource_prefix}-subnet"
  resource_group_name  = azurerm_resource_group.main.name
  virtual_network_name = azurerm_virtual_network.main.name
  address_prefixes     = ["10.0.1.0/24"]
}

# Network Security Group
resource "azurerm_network_security_group" "main" {
  name                = "${var.resource_prefix}-nsg"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  # SSH
  security_rule {
    name                       = "SSH"
    priority                   = 1001
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "22"
    source_address_prefix      = var.allowed_ssh_ip
    destination_address_prefix = "*"
  }

  # HTTP (Frontend)
  security_rule {
    name                       = "HTTP"
    priority                   = 1002
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "80"
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }

  # Backend API
  security_rule {
    name                       = "Backend-API"
    priority                   = 1003
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "8080"
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }

  # Frontend (alternative port)
  security_rule {
    name                       = "Frontend"
    priority                   = 1004
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_range     = "8081"
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }

  tags = {
    environment = "testing"
    project     = "todo-app"
  }
}

# Public IP
resource "azurerm_public_ip" "main" {
  name                = "${var.resource_prefix}-pip"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name
  allocation_method   = "Static"
  sku                 = "Standard"

  tags = {
    environment = "testing"
    project     = "todo-app"
  }
}

# Network Interface
resource "azurerm_network_interface" "main" {
  name                = "${var.resource_prefix}-nic"
  location            = azurerm_resource_group.main.location
  resource_group_name = azurerm_resource_group.main.name

  ip_configuration {
    name                          = "internal"
    subnet_id                     = azurerm_subnet.main.id
    private_ip_address_allocation = "Dynamic"
    public_ip_address_id          = azurerm_public_ip.main.id
  }

  tags = {
    environment = "testing"
    project     = "todo-app"
  }
}

# Associate NSG with NIC
resource "azurerm_network_interface_security_group_association" "main" {
  network_interface_id      = azurerm_network_interface.main.id
  network_security_group_id = azurerm_network_security_group.main.id
}

# Virtual Machine
resource "azurerm_linux_virtual_machine" "main" {
  name                = "${var.resource_prefix}-vm"
  resource_group_name = azurerm_resource_group.main.name
  location            = azurerm_resource_group.main.location
  size                = var.vm_size
  admin_username      = var.admin_username

  network_interface_ids = [
    azurerm_network_interface.main.id,
  ]

  admin_ssh_key {
    username   = var.admin_username
    public_key = tls_private_key.ssh.public_key_openssh
  }

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }

  source_image_reference {
    publisher = "Canonical"
    offer     = "ubuntu-24_04-lts"
    sku       = "server"
    version   = "latest"
  }

  tags = {
    environment = "testing"
    project     = "todo-app"
  }
}

# Generate Ansible inventory
resource "local_file" "ansible_inventory" {
  content = templatefile("${path.module}/../ansible/inventory.tmpl", {
    vm_ip          = azurerm_public_ip.main.ip_address
    admin_username = var.admin_username
    private_key    = "${path.module}/keys/id_rsa"
  })
  filename = "${path.module}/../ansible/inventory.ini"

  depends_on = [azurerm_linux_virtual_machine.main]
}

# Generate .env file for docker-compose
resource "local_file" "docker_env" {
  content = <<-EOT
    # Database Configuration
    POSTGRES_DB=${var.db_name}
    POSTGRES_USER=${var.db_user}
    POSTGRES_PASSWORD=${var.db_password}

    # Backend Configuration
    DB_HOST=db
    DB_PORT=5432
    DB_NAME=${var.db_name}
    DB_USER=${var.db_user}
    DB_PASSWORD=${var.db_password}
    ALLOWED_ORIGINS=http://${azurerm_public_ip.main.ip_address}:8081
    SPRING_PROFILES_ACTIVE=docker

    # Frontend Configuration
    VITE_API_URL=http://${azurerm_public_ip.main.ip_address}:8080
  EOT
  filename = "${path.module}/../ansible/files/.env"

  depends_on = [azurerm_linux_virtual_machine.main]
}

# Wait for VM to be ready
resource "null_resource" "wait_for_vm" {
  depends_on = [
    azurerm_linux_virtual_machine.main,
    local_file.private_key
  ]

  provisioner "local-exec" {
    command = "sleep 60"
  }
}

# Run Ansible playbook
resource "null_resource" "ansible_provision" {
  depends_on = [
    null_resource.wait_for_vm,
    local_file.ansible_inventory,
    local_file.docker_env
  ]

  provisioner "local-exec" {
    working_dir = "${path.module}/../ansible"
    command     = <<-EOT
      ANSIBLE_HOST_KEY_CHECKING=False ansible-playbook \
        -i inventory.ini \
        --private-key ${path.module}/keys/id_rsa \
        playbook.yml
    EOT
  }

  triggers = {
    always_run = timestamp()
  }
}
