# Learning-Github-Actions
Github Actions

## Kubernetes deploy prerequisites

The deployment workflow creates a `ghcr-pull-secret` in the target namespace before applying the manifests.

Add these GitHub repository or environment secrets before running the deploy job:

- `GHCR_USERNAME`: GitHub username or service account with access to the GHCR package.
- `GHCR_TOKEN`: Personal access token with package read access for GHCR.
