# Remove the old .git directory
Remove-Item -Recurse -Force .git

# Initialize a new repository
git init

# Add all files
git add .

# Create initial commit
git commit -m "Initial commit - Clean repository"

# Add the remote origin back
git remote add origin git@github.com:Joseph-Wamui/DMSProject.git

# Force push to main branch
git branch -M main
git push -f origin main
