const spawn = require('child_process').spawn
const path = require('path')
const JSONStream = require('JSONStream')

const DEFAULT_PATH_TO_SYBASE_DRIVER = path.join(__dirname, '../JavaSybaseLink/dist/JavaSybaseLink.jar')

function Sybase ({ host, port, database, userName, password, logTiming, pathToJavaBridge, dbCharset, odbcUrl }) {
  if (odbcUrl != null && (!!host || !!port || !!database || !!userName || !!password)) {
    throw new Error(`If 'odbcUrl' is set, no other connection properties can be passed (host, port, dbName, userName, password)`)
  }
  this.connected = false
  this.host = host
  this.port = port
  this.database = database
  this.userName = userName
  this.password = password
  this.logTiming = (logTiming === true)
  this.dbCharset = dbCharset || 'utf8'
  this.odbcUrl = odbcUrl

  this.pathToJavaBridge = pathToJavaBridge || DEFAULT_PATH_TO_SYBASE_DRIVER

  this.queryCount = 0
  this.currentMessages = {} // look up msgId to message sent and call back details.

  this.jsonParser = JSONStream.parse()
}

Sybase.prototype.connect = function (callback) {
  const that = this
  if (this.odbcUrl != null) {
    this.javaDB = spawn('java', ['-jar', this.pathToJavaBridge, this.odbcUrl])
  } else {
    this.javaDB = spawn('java', ['-jar', this.pathToJavaBridge, this.host, this.port, this.database, this.userName, this.password])
  }

  this.javaDB.stdout.once('data', function (data) {
    if ((data + '').trim() !== 'connected') {
      callback(new Error('Error connecting ' + data))
      return
    }

    that.javaDB.stderr.removeAllListeners('data')
    that.connected = true

    // set up normal listeners.
    that.javaDB.stdout.setEncoding(that.dbCharset).pipe(that.jsonParser).on('data', function (jsonMsg) {
      that.onSQLResponse(jsonMsg)
    })
    that.javaDB.stderr.on('data', function (err) {
      that.onSQLError(err)
    })

    callback(null, data)
  })

  // handle connection issues.
  this.javaDB.stderr.once('data', function (data) {
    that.javaDB.stdout.removeAllListeners('data')
    that.javaDB.kill()
    callback(new Error(data))
  })
}

Sybase.prototype.disconnect = function () {
  this.javaDB.kill()
  this.connected = false
}

Sybase.prototype.isConnected = function () {
  return this.connected
}

Sybase.prototype.query = function (sql, callback) {
  if (this.isConnected === false) {
    callback(new Error('Database isn\'t connected.'))
    return
  }
  const hrstart = process.hrtime()
  this.queryCount++

  const msg = {
    msgId: this.queryCount,
    sql,
    sentTime: (new Date()).getTime()
  }
  const strMsg = JSON.stringify(msg).replace(/[\n]/g, '\\n')
  msg.callback = callback
  msg.hrstart = hrstart

  this.currentMessages[msg.msgId] = msg

  this.javaDB.stdin.write(strMsg + '\n')
}

Sybase.prototype.onSQLResponse = function (jsonMsg) {
  let err = null
  const request = this.currentMessages[jsonMsg.msgId]
  delete this.currentMessages[jsonMsg.msgId]

  let result = jsonMsg.result
  if (result.length === 1) {
    result = result[0]
  } // if there is only one just return the first RS not a set of RS's

  const currentTime = (new Date()).getTime()
  const sendTimeMS = currentTime - jsonMsg.javaEndTime
  const hrend = process.hrtime(request.hrstart)
  const javaDuration = (jsonMsg.javaEndTime - jsonMsg.javaStartTime)

  if (jsonMsg.error !== undefined) {
    err = new Error(jsonMsg.error)
  }

  if (this.logTiming) {
    console.log('Execution time (hr): %ds %dms dbTime: %dms dbSendTime: %d sql=%s', hrend[0], hrend[1] / 1000000, javaDuration, sendTimeMS, request.sql)
  }
  request.callback(err, result)
}

Sybase.prototype.onSQLError = function (data) {
  const error = new Error(data)

  const callBackFunctions = this.currentMessages
    .map(message => message.callback)

  // clear the current messages before calling back with the error.
  this.currentMessages = []
  callBackFunctions.forEach(callback => callback(error))
}

module.exports = Sybase
