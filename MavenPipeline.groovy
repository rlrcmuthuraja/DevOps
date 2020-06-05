node
{
    // Checkout nodeproject from SCM
    stage('Checkout nodeproject')
    { 
        
        checkout([$class: 'GitSCM', branches: [[name: '*/nexus3_for_npm_libraries']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'XXX', url: 'ssh://git@172.22.105.155:7999/wat/nodeproject.git']]])

    }

    // Remove old nodeproject build from build machine
    stage ('Remove old nodeproject build ')
    {
        sh 'rm -rf dist'
        sh 'rm -f nodeproject.zip'
    }

    //Install nodeproject
    stage ('Install nodeproject')
    {
        nodejs('NodeJS 10.9.0')
        {
            sh 'npm install -verbose'
        }

    }

    //Build nodeproject
    stage ('Build nodeproject')
    {
        nodejs('NodeJS 10.9.0')
        {
             sh 'npm run build:prod'
        }
    }

    //Zip nodeproject
    stage ('Zip nodeproject')
    {
        sh 'cd dist;tar -czf ../nodeproject.tar.gz *;date'
    }

    //Deploy nodeproject as Maven in Nexus repository 'maven-releases'
    stage('Deploy nodeproject in Nexus')
    { 
        
       def mvnHome = tool name: 'maven 3.6.0', type: 'maven'
       def mvnCMD = "${mvnHome}/bin/mvn"
       sh "${mvnCMD} deploy:deploy-file -DgroupId=com -DartifactId=nodeproject -Dversion=17.12.7 -Dpackaging=tar.gz -DrepositoryId=maven-releases -Durl=http://00000.0000:0000/repository/maven-releases/ -Dfile=nodeproject.tar.gz"

    }

    //Checkout plotter-service Maven project
    stage('Checkout plotter-service')
    { 
        
        checkout([$class: 'GitSCM', branches: [[name: '*/plotter-service-nexus3']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'XXX', url: 'ssh://git@yyy.git']]])

    }

    //Deeploy plotter-service Maven project in Nexus repository 'Snapshots'
    stage('Deeploy plotter-service in Nexus')
    { 
        
       def mvnHome = tool name: 'maven 3.6.0', type: 'maven'
       def mvnCMD = "${mvnHome}/bin/mvn"
       sh "${mvnCMD} clean deploy" 
    }

    
    
}

