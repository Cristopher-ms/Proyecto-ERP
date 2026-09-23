# ==============================================================================
# Script de aprovisionamiento de recursos en Microsoft Azure para el ERP
# Requisitos: Azure CLI instalado (az login previo)
# ==============================================================================

$RESOURCE_GROUP = "rg-erp-prod"
$LOCATION = "eastus"
$ACR_NAME = "acrerp" + (Get-Random -Minimum 1000 -Maximum 9999)
$ENVIRONMENT_NAME = "env-erp-prod"
$STATIC_WEB_APP = "stapp-erp-frontend"

Write-Host ">>> 1. Creando Grupo de Recursos en Azure: $RESOURCE_GROUP..." -ForegroundColor Cyan
az group create --name $RESOURCE_GROUP --location $LOCATION

Write-Host ">>> 2. Creando Azure Container Registry (ACR): $ACR_NAME..." -ForegroundColor Cyan
az acr create --resource-group $RESOURCE_GROUP --name $ACR_NAME --sku Basic --admin-enabled true

Write-Host ">>> 3. Creando Entorno para Azure Container Apps: $ENVIRONMENT_NAME..." -ForegroundColor Cyan
az containerapp env create --name $ENVIRONMENT_NAME --resource-group $RESOURCE_GROUP --location $LOCATION

Write-Host ">>> 4. Creando Azure Static Web App para el Frontend de Angular..." -ForegroundColor Cyan
az staticwebapp create `
  --name $STATIC_WEB_APP `
  --resource-group $RESOURCE_GROUP `
  --location "eastus2" `
  --source "https://github.com/Cristopher-ms/Proyecto-ERP" `
  --branch "main" `
  --app-location "frontend/erp-web" `
  --output-location "dist/erp-web/browser" `
  --login-with-github

Write-Host "==============================================================================" -ForegroundColor Green
Write-Host "Infraestructura inicial en Azure aprovisionada exitosamente." -ForegroundColor Green
Write-Host "ACR Creado: $ACR_NAME" -ForegroundColor Yellow
Write-Host "==============================================================================" -ForegroundColor Green
