def call (Closure... contexts) {
    pipeline {
    agent any
    environment {
        PROJECT_URL = 'https://github.com/chillo-tech/formation-jenkins-hello-world.git'
        CREDENTIALS_ID = "chillo-tech"
    }

    parameters {
        choice(name: "environment", choices: ["test", "preprod", "prod"], description: "Environnement sur lequel déployer le livrable")
    }

    stages {
        stage('Cloner le répertoire') {
            steps {
                checkout([
                    $class: 'GitSCM', 
                    branches: [[name: "main"]],
                    userRemoteConfigs: [[
                        credentialsId: "${CREDENTIALS_ID}",
                        url: "${PROJECT_URL}",
                    ]]
                ])
                sh "printenv"
            }
           
        }

        stage('Build Project') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw clean package spring-boot:repackage -Dmaven.test.skip=true'
            }
        }

        stage('Test') {
            steps {
                sh 'ls -al target'
            }
        }

        stage('Deploy') {
            steps {
                echo "Environnement ${params.environment}"
                sshPublisher(
                    publishers: [
                        sshPublisherDesc(
                            configName: 'training-server',  // correspond au Nom de la configuration
                            transfers: [
                                sshTransfer(
                                    sourceFiles: 'target/*.jar',
                                    remoteDirectory: '/tmp',
                                     execCommand: '''
                                        ls -lh target

                                    '''
                                )
                            ],
                              usePromotionTimestamp: false,
                                                    verbose: true
                        )
                    ]
                )
            }
        }
        
    }

    post {
        success {
           sh "echo success" 
        }
        failure {
           sh "echo failure" 
        }
        cleanup {
           sh "echo cleanup" 
        }
    }
}
}