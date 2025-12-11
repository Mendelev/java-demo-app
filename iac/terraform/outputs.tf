output "resource_group_name" {
  description = "Name of the resource group"
  value       = azurerm_resource_group.main.name
}

output "vm_name" {
  description = "Name of the virtual machine"
  value       = azurerm_linux_virtual_machine.main.name
}

output "public_ip" {
  description = "Public IP address of the VM"
  value       = azurerm_public_ip.main.ip_address
}

output "ssh_command" {
  description = "SSH command to connect to the VM"
  value       = "ssh -i ${path.module}/keys/id_rsa ${var.admin_username}@${azurerm_public_ip.main.ip_address}"
}

output "frontend_url" {
  description = "URL to access the frontend application"
  value       = "http://${azurerm_public_ip.main.ip_address}:8081"
}

output "backend_url" {
  description = "URL to access the backend API"
  value       = "http://${azurerm_public_ip.main.ip_address}:8080"
}

output "private_key_path" {
  description = "Path to the SSH private key"
  value       = "${path.module}/keys/id_rsa"
}
