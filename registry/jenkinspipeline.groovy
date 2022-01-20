timeout(time: 5, unit: 'MINUTES'){
  dir('psi-registry'){

    try {
      stage('check out app'){
        git url: 'git@git.ncei.noaa.gov:onestop/onestop.git'
      }

      stage('build'){
        try {
          sh "../gradlew build -x test -x BuildDockerImage -x integrationTest -x dependencyCheckAggregate"
        } finally {
          step([$class: 'JUnitResultArchiver', testResults: '**/build/test-results/test/TEST-*.xml'])
        }
      }
    } catch (err) {
      step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: "agile.foam-cat@noaa.gov", sendToIndividuals: true])
      throw err
    }
  }
}