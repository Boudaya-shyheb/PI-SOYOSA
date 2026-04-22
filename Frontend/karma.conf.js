// Karma configuration file, see link for more information
// https://karma-runner.github.io/1.0/config/configuration-file.html

module.exports = function (config) {
  // Set Opera GX path for Windows
  if (process.platform === 'win32') {
    const fs = require('fs');
    const path = require('path');
    const baseDir = 'C:\\Users\\LENOVO\\AppData\\Local\\Programs\\Opera GX';
    let foundPath = path.join(baseDir, 'launcher.exe');
    
    if (!fs.existsSync(foundPath)) {
      // Look for versioned folders like 129.0.0.0...
      try {
        const folders = fs.readdirSync(baseDir);
        for (const folder of folders) {
          const versionPath = path.join(baseDir, folder, 'launcher.exe');
          if (fs.existsSync(versionPath)) {
            foundPath = versionPath;
            break;
          }
        }
      } catch (e) {}
    }
    process.env.OPERA_BIN = foundPath;
  }

  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-opera-launcher'),
      require('karma-jasmine-html-reporter'),
      require('karma-coverage'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client: {
      jasmine: {
        // you can add configuration options for Jasmine here
        // the possible options are listed at https://jasmine.github.io/api/edge/Configuration.html
        // for example, you can disable the random execution with `random: false`
        // or set a specific seed with `seed: 4321`
      },
      clearContext: false // leave Jasmine Spec Runner output visible in browser
    },
    jasmineHtmlReporter: {
      suppressAll: true // removes the duplicated traces
    },
    coverageReporter: {
      dir: require('path').join(__dirname, './coverage/jungle-in-english-angular'),
      subdir: '.',
      reporters: [
        { type: 'html' },
        { type: 'text-summary' }
      ]
    },
    reporters: ['progress', 'kjhtml'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: true,
    customLaunchers: {
      OperaGX: {
        base: 'Opera',
        // Common paths for Opera GX on Windows
        // The karma-opera-launcher looks for OPERA_BIN
        flags: []
      }
    },
    browsers: ['OperaGX'],
    singleRun: false,
    restartOnFileChange: true
  });
};
