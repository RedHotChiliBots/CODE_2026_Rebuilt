for branch in $(git branch --list); do
    git checkout $branch
    git pull
done
# After the loop finishes, you might want to return to your original branch, e.g., 'main' or 'master'
git checkout main

