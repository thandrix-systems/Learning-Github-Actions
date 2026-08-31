# Learning-Github-Actions
Github Actions

## Kubernetes deploy prerequisites

The deployment workflow creates a `dockerhub-pull-secret` in the target namespace before applying the manifests.

Add these GitHub repository or environment secrets before running the deploy job:

- `DOCKERHUB_USERNAME`: Docker Hub username with access to the image repository.
- `DOCKERHUB_PASSWORD`: Docker Hub password or access token with pull permissions.
