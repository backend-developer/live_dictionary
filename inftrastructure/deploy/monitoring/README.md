# build docker image for deploy build monitoring

docker build -t terra-deploy-monitoring-of-builds .
docker run --entrypoint sh terra-deploy-monitoring-of-builds -c "terraform init; terraform plan /"
