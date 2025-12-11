Project Plan: Jenkins to Harness Migration Simulation

1. Project Overview

Goal: Create a local laboratory to simulate a CI/CD pipeline migration from Jenkins to Harness.
Scenario: A Java + Maven application currently deployed via Jenkins (Build -> Push -> SSH Deploy).
Target State: A Harness Pipeline using a Local Delegate to deploy to the same infrastructure.
Learning Objective: Understand the role of the Harness Delegate, the translation of Jenkinsfiles to Harness YAML, and the SSH deployment mechanism.

2. Infrastructure Architecture (The Lab)

Since we are running this locally, we will use Docker to simulate the servers. We will create a dedicated Docker Network so the containers can resolve each other by name.

Components

Host Machine: Your computer (holds the Source Code).

Jenkins Container: Simulates the legacy CI server.

Target VM Container: A simple Ubuntu container with SSH enabled. This simulates the "Production Server."

Harness Delegate Container: The bridge connecting Harness SaaS to your local Docker network.

3. Phase 1: The "Legacy" Setup (Jenkins)

Goal: Establish the baseline. You cannot migrate what doesn't exist.

Step 1.1: The Network & Containers

Create a docker-compose.yml file to spin up Jenkins and the Target VM.

Network: Create a bridge network named migration-lab.

Jenkins: Use jenkins/jenkins:lts. Mount var/run/docker.sock so Jenkins can run Docker commands (Docker-in-Docker technique).

Target VM: Use a custom Dockerfile based on Ubuntu. Install openssh-server and openjdk-17-jre (to run the app). Set a root password or generic user (e.g., user:password).

Step 1.2: Jenkins Configuration

Unlock Jenkins: Access localhost:8080, get the initial admin password from the container logs.

Install Plugins: Install "Suggested Plugins" + "Docker Pipeline" + "SSH Agent".

Credentials:

Add your Docker Hub credentials (username/password).

Add the SSH credentials for the Target VM.

4. Phase 2: The "Legacy" Pipeline

Goal: Create a working Jenkinsfile.

Since you are new to Jenkins, we will use a Declarative Pipeline. Create a file named Jenkinsfile in your Java project root.

Pipeline Stages

Checkout: (Implicit in Jenkins).

Build (Maven): Run mvn clean package.

Build Image: Use Docker to build the image tagged with the build number.

Push: Push the image to Docker Hub (acting as Artifactory).

Deploy (SSH):

Jenkins SSHs into the target-vm container.

Stops any running instance of the container.

Runs docker run -d -p 80:8080 <your-image>.

Checkpoint: Run this job in Jenkins. If your Java app is accessible at localhost:80 (mapped from the Target VM), Phase 2 is complete.

5. Phase 3: The Harness Bridge (Delegate)

Goal: Allow Harness SaaS to touch your local Docker network.

Step 3.1: Install the Delegate

In Harness, go to Project Settings > Delegates.

Select Docker Delegate.

Crucial: When running the Delegate command, ensure you attach it to the same Docker network as your other containers:
--network migration-lab

Verify the Delegate is "Connected" (Green) in the Harness UI.

6. Phase 4: The Migration Strategy

Goal: Replicate the Jenkins logic in Harness.

Step 4.1: Connectors

Create the following connectors in Harness, managed by the Local Delegate:

Docker Registry: Connect to Docker Hub.

SSH Credential: Create a "Username/Password" secret for the Target VM.

Infrastructure Definition: Define a "Physical Data Center" environment.

Host: target-vm (Since they share the docker network, the Delegate can find it by container name).

Step 4.2: The "Migrate" Plugin approach (Simulation)

In Harness, select "Import from Jenkins" (if available in your edition) or use the YAML builder.

Reality Check: The automated tool usually handles the Build/Push stages well. It often fails on complex Scripted SSH deployment logic.

Expectation: You will likely need to manually refine the "Deploy" stage.

Step 4.3: Constructing the Pipeline

Stage 1: Build & Push

Use the "Build and Push to Docker Registry" step.

Stage 2: Deploy

Method: SSH / Command Step.

Script:

docker pull <image_name>
docker stop my-app || true
docker rm my-app || true
docker run -d --name my-app -p 8080:8080 <image_name>


7. Phase 5: Verification & Cutover

Goal: Prove the new pipeline works.

Trigger: Run the Harness Pipeline manually.

Monitor: Watch the "Console View" in Harness. You should see logs streaming from your local Delegate.

Verify: Check the Target VM. The app should have restarted with the new version.

Compliance Check: Confirm the deployment happened via the Delegate, satisfying the "Local VM" requirement.

8. Required Resources / Tools Checklist

Tool

Purpose

Note

Docker Desktop

Hosts the Lab

Ensure "Expose daemon on tcp://localhost:2375" is checked if on Windows.

Java 17+ / Maven

Build Tool

Installed locally for testing.

Git

SCM



Harness Account

CD Platform

Free tier is sufficient.

Docker Hub Account

Registry

Acts as the "Artifactory".

9. Simulation Timeline (Estimated)

Day 1: Docker Setup & Jenkins Configuration. (Getting the docker-compose networking right usually takes the most time).

Day 2: Writing the Jenkinsfile and getting a successful "Legacy" deploy.

Day 3: Delegate Installation & Harness Pipeline Creation.