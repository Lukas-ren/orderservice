FROM ubuntu:latest
LABEL authors="lukasduran"

ENTRYPOINT ["top", "-b"]