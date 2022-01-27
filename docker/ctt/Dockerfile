FROM openjdk:8
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends maven libxtst6 libgl1-mesa-glx libxt6 libglu1-mesa libsm6 libxrender1
COPY . /root/nanosat-mo-framework
WORKDIR /root/nanosat-mo-framework
ENV NMF_HOME=/root/nanosat-mo-framework
RUN mkdir -p /root/.ops-sat-simulator
RUN mvn install
