@echo off
echo Initializing Git repository and pushing to GitHub...

cd /d "c:\Documents\Education\Ecommerce_grievance\Ecommerce_grievance\Ecommerce_grievance"

echo Adding all files to Git...
git add .

echo Committing changes...
git commit -m "Initial commit: ResolveIT E-commerce Grievance System with modern UI and security features"

echo Setting up remote repository...
git remote add origin https://github.com/lokitha-muni/resolveIT-ecommerce-grievance.git

echo Pushing to GitHub...
git push -u origin main

echo Deployment complete!
pause