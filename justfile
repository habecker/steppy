default:
    just --list

report:
    mvn jacoco:report && open target/site/jacoco/index.html

test:
    mvn clean verify -Dstage=test -Dcheckstyle.skip=true
    mvn jacoco:report

docs-serve:
    mkdocs serve

docs-build:
    mkdocs build

docs-deploy:
    mkdocs gh-deploy
