#!/usr/bin/groovy
// load pipeline functions
//@Library('Pipeline@master')
//def pipeline = new io.helm.Pipeline()
podTemplate(label: 'helmpipe', containers: [
    containerTemplate(name: 'helm', image: 'dtzar/jnlp-slave-helm:latest', ttyEnabled: true),
  ])
{
  node ('helmpipe'){
  def pwd = pwd()
  def workDir = "${pwd}/pdxazure/"
  def chart_dir = "${pwd}/pdxcharts/incubator/pdxazure"

  checkout scm

  // read in required jenkins workflow config values
  def inputFile = readFile('Jenkinsfile.json')
  def config = new groovy.json.JsonSlurperClassic().parseText(inputFile)
  println "pipeline config ==> ${config}"

  // continue only if pipeline enabled
  if (!config.pipeline.enabled) {
      println "pipeline disabled"
      return
  }

  // set additional git envvars for image tagging
  //pipeline.gitEnvVars()

  // used to debug deployment setup
  //env.DEBUG_DEPLOY = true

  // debugging helm deployments
  //if (env.DEBUG_DEPLOY) {
    println "Runing helm tests"
    //pipeline.kubectlTest()
    // Test that kubectl can correctly communication with the Kubernetes API
    echo "running kubectl test"
    sh "kubectl get nodes"
    //pipeline.helmConfig()
    //setup helm connectivity to Kubernetes API and Tiller
    sh "helm init"
    sh "helm version"
  //}

  //def acct = pipeline.getContainerRepoAcct(config)

  // tag image with version, and branch-commit_id
  //def image_tags_map = pipeline.getContainerTags(config)

  // compile tag list
  //def image_tags_list = pipeline.getMapValues(image_tags_map)

  stage ('preparation') {

    // Print env -- debugging
    //sh "env | sort"

    sh "mkdir -p ${workDir}"
    sh "cp -R ${pwd}/* ${workDir}"

  }

  stage ('compile') {

    sh "cd ${workDir}"
    sh "gradlew"

  }

  stage ('test') {

    // run tests
    echo "tests would go here"

    // run helm chart linter
    //pipeline.helmLint(chart_dir)
    sh "helm lint ${chart_dir}"

    // run dry-run helm chart installation
    /* pipeline.helmDeploy(
      dry_run       : true,
      name          : config.app.name,
      version_tag   : image_tags_list.get(0),
      chart_dir     : chart_dir,
      replicas      : config.app.replicas,
      cpu           : config.app.cpu,
      memory        : config.app.memory
    ) */
    sh "helm upgrade --dry-run --install ${args.name} ${args.chart_dir} --set ImageTag=${args.version_tag},Replicas=${args.replicas},Cpu=${args.cpu},Memory=${args.memory} --namespace=${args.name}"

  }

  stage ('publish') {

    // perform docker login to acr as the docker-pipeline-plugin doesn't work with the next auth json format
    withCredentials([[$class          : 'UsernamePasswordMultiBinding', credentialsId: config.container_repo.jenkins_creds_id,
                    usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
      sh "docker login -e ${config.container_repo.dockeremail} -u ${env.USERNAME} -p ${env.PASSWORD} nikeacr.azurecr.io"
    }

    // build and publish container
    /*pipeline.containerBuildPub(
        dockerfile: config.container_repo.dockerfile,
        host      : config.container_repo.host,
        acct      : acct,
        repo      : config.container_repo.repo,
        tags      : image_tags_list,
        auth_id   : config.container_repo.jenkins_creds_id
    )*/

  }

  // deploy only the master branch
  //if (env.BRANCH_NAME == 'master') {
    stage ('deploy') {
      echo "deploy phase"
      // Deploy using Helm chart
    /*  pipeline.helmDeploy(
        dry_run       : false,
        name          : config.app.name,
        version_tag   : image_tags_list.get(0),
        chart_dir     : chart_dir,
        replicas      : config.app.replicas,
        cpu           : config.app.cpu,
        memory        : config.app.memory
      )*/

    }
  //}
}
} //end podTemplate