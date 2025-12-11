cd iac/terraform

# Initialize Terraform
terraform init

# Preview changes
terraform plan

# Deploy (creates VM, generates SSH keys, runs Ansible)
terraform apply

# After deployment, outputs will show:
# - SSH command to connect
# - Frontend URL (http://<IP>:8081)
# - Backend URL (http://<IP>:8080)