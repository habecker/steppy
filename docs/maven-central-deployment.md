# Maven Central Deployment Guide

This guide explains how to deploy Steppy to Maven Central (via OSSRH - Open Source Software Repository Hosting).

## Prerequisites

### 1. OSSRH Account Setup

1. **Create an OSSRH account**:
   - Go to [https://s01.oss.sonatype.org/](https://s01.oss.sonatype.org/)
   - Click "Sign Up" and create an account
   - Verify your email address

2. **Create a JIRA ticket**:
   - Go to [https://issues.sonatype.org/](https://issues.sonatype.org/)
   - Create a new ticket with type "New Project"
   - Provide the following information:
     - Group ID: `de.y2g`
     - Project URL: `https://github.com/habecker/steppy`
     - SCM URL: `https://github.com/habecker/steppy.git`
     - Username: Your OSSRH username

3. **Wait for approval**:
   - OSSRH will review and approve your request
   - You'll receive an email confirmation

### 2. GPG Key Setup

1. **Generate a GPG key** (if you don't have one):
   ```bash
   gpg --gen-key
   ```

2. **Export your public key**:
   ```bash
   gpg --armor --export your-email@example.com > public-key.asc
   ```

3. **Upload your public key**:
   - Go to [https://keyserver.ubuntu.com/](https://keyserver.ubuntu.com/)
   - Upload your public key

4. **Export your private key** (for GitHub Actions):
   ```bash
   gpg --armor --export-secret-key your-email@example.com
   ```

### 3. GitHub Secrets Configuration

Add the following secrets to your GitHub repository:

1. **OSSRH_USERNAME**: Your OSSRH username
2. **OSSRH_PASSWORD**: Your OSSRH password
3. **GPG_PRIVATE_KEY**: Your exported private GPG key (including the `-----BEGIN PGP PRIVATE KEY BLOCK-----` and `-----END PGP PRIVATE KEY BLOCK-----` lines)
4. **GPG_PASSPHRASE**: The passphrase for your GPG key

## Configuration Changes

The following changes have been made to support Maven Central deployment:

### pom.xml Updates

1. **Added project metadata**:
   - Name, description, URL
   - License information (MIT)
   - Developer information
   - Encoding properties

2. **Updated distribution management**:
   - Changed from GitHub Packages to OSSRH
   - Configured staging and snapshot repositories

3. **Added required plugins**:
   - `maven-javadoc-plugin`: Generates Javadoc JAR
   - `maven-gpg-plugin`: Signs artifacts with GPG
   - `nexus-staging-maven-plugin`: Handles staging and release

### GitHub Actions Workflow

A new workflow file `maven-central-release.yml` has been created that:
- Triggers on version tags (`v*`)
- Sets up GPG signing
- Configures OSSRH authentication
- Deploys to Maven Central staging
- Creates GitHub releases

## Deployment Process

### 1. Prepare for Release

1. **Update version** in `pom.xml`:
   ```xml
   <version>0.3.0</version>
   ```

2. **Commit and push changes**:
   ```bash
   git add .
   git commit -m "Prepare release 0.3.0"
   git push
   ```

### 2. Create and Push Tag

```bash
git tag v0.3.0
git push origin v0.3.0
```

### 3. Monitor Deployment

1. **Check GitHub Actions**: Monitor the workflow execution
2. **Check OSSRH**: Visit [https://s01.oss.sonatype.org/](https://s01.oss.sonatype.org/) to see your staging repository
3. **Release**: The artifacts will be automatically released to Maven Central

### 4. Verify Deployment

After successful deployment, your artifacts will be available on Maven Central:
- [https://repo1.maven.org/maven2/de/y2g/steppy/](https://repo1.maven.org/maven2/de/y2g/steppy/)

## Troubleshooting

### Common Issues

1. **GPG signing fails**:
   - Ensure your GPG private key is correctly exported
   - Verify the passphrase is correct
   - Check that the key is available in the GitHub Actions environment

2. **OSSRH authentication fails**:
   - Verify your OSSRH username and password
   - Ensure your account has been approved for the group ID

3. **Staging fails**:
   - Check that all required metadata is present in `pom.xml`
   - Verify that source and Javadoc JARs are generated
   - Ensure all tests pass

### Manual Deployment

If automated deployment fails, you can deploy manually:

```bash
# Set version
mvn versions:set -DnewVersion=0.3.0

# Deploy to staging
mvn clean deploy

# Close and release staging repository
mvn nexus-staging:close
mvn nexus-staging:release
```

## Maintenance

### Updating Dependencies

When updating dependencies, ensure they are compatible with Maven Central requirements:
- Use stable, released versions
- Avoid snapshot dependencies in releases
- Verify license compatibility

### Version Management

- Use semantic versioning (e.g., `1.2.3`)
- Create tags for all releases
- Update version in `pom.xml` before tagging

## Additional Resources

- [OSSRH Guide](https://central.sonatype.org/pages/ossrh-guide.html)
- [Maven Central Requirements](https://central.sonatype.org/pages/requirements.html)
- [GPG Signing Guide](https://central.sonatype.org/pages/working-with-pgp-signatures.html)

