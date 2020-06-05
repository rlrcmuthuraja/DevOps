import hudson.model.*
def des = 'UNKNOWN'

pipeline {
  agent any
  tools {
    maven 'maven 3.6.0'
    nodejs 'NodeJS 10.9.0'
    jdk 'openjdk11'
  }
  stages {
        stage('CHECKOUT'){
                       steps {
                                  //checkout([$class: 'GitSCM', branches: [[name: '*/for_pipeline']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'WipeWorkspace']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'XXXX', url: 'ssh://git@yyy.git']]])
                                  //checkout([$class: 'GitSCM', branches: [[name: '*/for_pipeline']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'XXXX', url: 'ssh://git@yyy.git']]])
                                  checkout([$class: 'GitSCM', branches: [[name: '*/for_pipeline']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'WipeWorkspace']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'XXXX', url: 'ssh://git@yyy.git']]])
                                  //  checkout([$class: 'GitSCM', branches: [[name: '*/for_pipeline']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'WipeWorkspace']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'Bitbucket_jenkins_user', url: 'ssh://git@yyy.git']]])
                                  //echo "current build number: ${currentBuild.number}"

                                  //sh 'npm run version'
                       }


        }
        stage('NPM INSTALL'){
                       steps {
                               sh 'rm -rf software.tar.gz'
                               sh 'rm -rf software-with-map.tar.gz'
                               sh 'npm install -verbose'
                             }
        }

        stage('NPM TEST') {
                        options {timeout(time: 60, unit: 'SECONDS')}
                        //input { message "Should we continue?" }
                        steps {
                                sh 'CI=true npm run test-ci'
                        }
        }


        stage('NPM BUILD') {
                             steps {
                             sh 'npm run build'
                             }

        }



    stage('DEPLOY') {
      steps {


        script {
                  des = currentBuild.rawBuild.getCauses()[0].getShortDescription()

                       if ( des.contains('user')){
                            stage ('RELEASE') {
                                    echo "TRIGGERED BY AN USER"

                                    //With '*.map' files
                                    sh 'cd build;tar -czf ../software-with-map.tar.gz *;date'
                                    //Without '*.map' files
                                    sh 'cd build;tar --exclude=*.map -czf ../software.tar.gz *;date'
                                    sh 'npm run deploy-release'
                                    sh 'npm run deploy-release-map'
                                    // sh 'npm run version'
                                    // echo "current build number: ${currentBuild.number}"
                                    // echo "version: ${version}"

                                     // BUMP VERSION
                                     checkout([$class: 'GitSCM', branches: [[name: '*/for_pipeline']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'WipeWorkspace']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'XXXX', url: 'ssh://git@yyy.git']]])
                                     sh "git checkout -b for_pipeline"
                                     echo '-------------------------------------------------'
                                     sh 'npm version patch'
                                     echo '-------------------------------------------------'
                                     sh "git push origin for_pipeline"

                            }
                       }
                       if ( des.contains('timer')){
                            stage ('NIGHTLY') {
                                    echo "TRIGGERED BY TIMER"


                                    //With '*.map' files
                                    sh 'cd build;tar -czf ../software-with-map.tar.gz *;date'
                                    //Without '*.map' files
                                    sh 'cd build;tar --exclude=*.map -czf ../software.tar.gz *;date'
                                    sh 'npm run deploy-release'
                                    sh 'npm run deploy-release-map'
                                    // sh 'npm run version'
                                    // echo "current build number: ${currentBuild.number}"
                                    // echo "version: ${version}"

                                    // BUMP VERSION
                                    checkout([$class: 'GitSCM', branches: [[name: '*/for_pipeline']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'WipeWorkspace']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'XXXX', url: 'ssh://git@yyy.git']]])
                                    sh "git checkout -b for_pipeline"
                                    echo '-------------------------------------------------'
                                    sh 'npm version patch'
                                    echo '-------------------------------------------------'
                                    sh "git push origin for_pipeline"

                            }
                       }
                       if ( des.contains('SCM')){
                            stage ('COMMIT') {
                                    echo "TRIGGERED BY SCM CHANGE"
                            }
                       }

               }

        echo "Description : ${des}"
      }
    }

  }



  post {
          success {
              echo 'whole pipeline successful'
              // To use this Teams notification, Add a team in Teams. Go to store, add jenkins and copy the url provided by Teams, And use that url here.
              //Success status can be enabled if needed.
              //office365ConnectorSend color: 'Blue', message: '', status: 'SUCCESS', webhookUrl: 'https://outlook.office.com/webhook/76013ff4-81ea-436a-8086-3406fee5b60a@9179d01a-e94c-4488-b5f0-4554bc474f8c/JenkinsCI/2202ec5fa2484f4583c6bf79ed5ee39b/d4e57ecb-60df-4e59-9b8a-e444cea46ec2'
          }
          failure {
              step([
                $class: 'Mailer',
                //recipients: ['muthu.ramachandran@technipfmc.com', emailextrecipients([[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']])].join(' ')
                recipients: [emailextrecipients([[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']])].join(' ')
              ])

          office365ConnectorSend color: 'Red', message: '' , status: 'FAILED', webhookUrl: 'https://outlook.office.com/webhook/76013ff4-81ea-436a-8086-3406fee5b60a@9179d01a-e94c-4488-b5f0-4554bc474f8c/JenkinsCI/2202ec5fa2484f4583c6bf79ed5ee39b/d4e57ecb-60df-4e59-9b8a-e444cea46ec2'
         }
  }

}





