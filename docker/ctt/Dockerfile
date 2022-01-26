FROM openjdk:8
ENV NMF_HOME=/workspaces/rl-project/nanosat-mo-framework
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends maven
COPY . /usr/src/app/  
WORKDIR /usr/src/app/
RUN mvn install
RUN apt-get -y install --no-install-recommends libxtst6 libgl1-mesa-glx libxt6 libglu1-mesa libsm6 libxrender1