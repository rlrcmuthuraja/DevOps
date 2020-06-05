pipeline {
            agent any

            /*agent {
              label 'windows slave FME'
            } */

            environment { FM3LIB = '/var/jenkins_home/workspace/CPP_library'}
            tools {
                maven 'maven 3.6.0'
                jdk 'jdk-8u192'
            }

            stages
                    {

                        stage('CHECKOUT')
                                {
                                    steps {
                                        checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'XXXX', url: 'ssh://git@engine.git']]])

                                    }
                                }
                        stage('CMAKE Release BUILD')
                                {
                                    steps {  cmakeBuild buildDir: '/var/jenkins_home/workspace/engine-SRC-from-SCM/build/gccrelease', buildType: 'Release', cleanBuild: true, installation: 'InSearchPath', sourceDir: '/var/jenkins_home/workspace/engine-SRC-from-SCM', steps: [[withCmake: true]]
                                    }
                                }

                        stage('CMAKE Debug BUILD')
                                 {
                                     steps {  cmakeBuild buildDir: '/var/jenkins_home/workspace/engine-SRC-from-SCM/build/gccdebug', buildType: 'Debug', cleanBuild: true, installation: 'InSearchPath', sourceDir: '/var/jenkins_home/workspace/engine-SRC-from-SCM', steps: [[withCmake: true]]
                                     }
                                 }



                       /* stage('COPY FILE TO ROOT')
                                {
                                    steps {  sh ' cd /var/jenkins_home/workspace/CPP_ENGINE/;rm -rf libcm_model.so;cp /var/jenkins_home/workspace/CPP_ENGINE/build/src/main/cxx/LLib/libcm_model.so /var/jenkins_home/workspace/CPP_ENGINE/'
                                    }

                                } */

                        stage('CMAKE TEST RELEASE')
                                {
                                    steps {  ctest arguments: '-V', installation: 'InSearchPath', workingDir: '/var/jenkins_home/workspace/engine-SRC-from-SCM/build/gccrelease'
                                    }
                                }

                       stage('CMAKE TEST DEBUG')
                                 {
                                     steps {  ctest arguments: '-V', installation: 'InSearchPath', workingDir: '/var/jenkins_home/workspace/engine-SRC-from-SCM/build/gccdebug'
                                      }
                                 }


                       stage ('compress')
                                {
                                   steps {  sh 'cd /var/jenkins_home/workspace/engine-SRC-from-SCM/build/gccrelease/;rm -rf libcm_model.tar.gz;tar -czvf libcm_model.tar.gz bin/libFmEnginePipe.so' }
                                   //steps {  sh 'cd /var/jenkins_home/workspace/engine-SRC-from-SCM/;rm -rf libcm_model.tar.gz;tar -czvf libcm_model.tar.gz /var/jenkins_home/workspace/engine-SRC-from-SCM/build/src/main/cxx/LLib/libcm_model.so' }
                                }

                       stage('send package')
                                {
                                    steps { sh "mvn deploy:deploy-file -DgroupId=com -DartifactId=fme -Dversion=2.0 -Dpackaging=tar.gz -DrepositoryId=maven-releases -Durl=http://000.000:0000/repository/maven-releases/ -Dfile=/var/jenkins_home/workspace/engine-SRC-from-SCM/build/gccrelease/libcm_model.tar.gz" }
                                    //steps { sh "mvn deploy:deploy-file -DgroupId=com -DartifactId=fme -Dversion=2.0 -Dpackaging=tar.gz -DrepositoryId=maven-releases -Durl=http://000.000:0000/repository/maven-releases/ -Dfile=/var/jenkins_home/workspace/engine-SRC-from-SCM/libcm_model.tar.gz" }

                                }
                    }


            post{
                            success {
                                echo 'whole pipeline successful'
                                office365ConnectorSend color: 'Blue', message: '', status: 'SUCCESS', webhookUrl: 'https://outlook.office.com/URL'
                            }
                            failure {
                                step([
                                  $class: 'Mailer',
                                   recipients: [emailextrecipients([[$class: 'DevelopersRecipientProvider'], [$class: 'RequesterRecipientProvider']])].join(' ')
                                ])
                                 office365ConnectorSend color: 'Red', message: '' , status: 'FAILED', webhookUrl: 'https://outlook.office.com/URL'

                            }
             }

        }

