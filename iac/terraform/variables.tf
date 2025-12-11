variable "resource_prefix" {
  description = "Prefix for all resource names"
  type        = string
  default     = "todo-app"
}

variable "location" {
  description = "Azure region for resources"
  type        = string
  default     = "Chile Central"
}

variable "admin_username" {
  description = "Admin username for the VM"
  type        = string
  default     = "azureuser"
}

variable "vm_size" {
  description = "Size of the Azure VM"
  type        = string
  default     = "Standard_D2ads_v5"
}

variable "allowed_ssh_ip" {
  description = "IP address allowed to SSH (use '*' for any, or your IP for security)"
  type        = string
  default     = "*"
}

variable "db_name" {
  description = "PostgreSQL database name"
  type        = string
  default     = "todoapp"
}

variable "db_user" {
  description = "PostgreSQL username"
  type        = string
  default     = "todo"
}

variable "db_password" {
  description = "PostgreSQL password"
  type        = string
  default     = "todo"
  sensitive   = true
}
