# Steppy

Steppy is a Java library for creating workflows. It is designed to be simple and easy to use.

## Documentation

The documentation is built with [MkDocs](https://www.mkdocs.org/) using the Material theme and is automatically deployed to [GitHub Pages](https://habecker.github.io/steppy/).

### Local Development

If you have [just](https://github.com/casey/just) installed, you can use these commands:

```bash
# Serve docs locally for development
just docs-serve

# Build static site
just docs-build

# Deploy to GitHub Pages (requires write access)
just docs-deploy
```

Or use MkDocs directly:

```bash
# Install dependencies
pip install -r requirements.txt

# Serve docs locally
mkdocs serve

# Build static site
mkdocs build
```

The HTML output is written to the `site/` directory. The documentation is automatically built and deployed to GitHub Pages on every push to the main branch.
