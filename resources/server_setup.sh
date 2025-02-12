mkdir test-repo
cd test-repo
git init
echo "Server Setup" > README.md
git add README.md
git commit -m "Initial commit"

git checkout -b feature-branch
echo "Feature 1" >> feature.txt
git add feature.txt
git commit -m "Add feature 1"

# Get and save the commit hash
COMMIT_HASH=$(git rev-parse HEAD)
echo $COMMIT_HASH > ../commit_hash.txt

# Get and save the absolute path
REPO_PATH=$(pwd)
echo $REPO_PATH > ../repo_path.txt