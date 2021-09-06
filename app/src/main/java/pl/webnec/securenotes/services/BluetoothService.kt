package pl.webnec.securenotes.services

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import pl.webnec.securenotes.utilities.BT
import pl.webnec.securenotes.utilities.TAG
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class BluetoothService(applicationContext: Context){

    private val SECURED_NAME = "ExampleName"
    private val SECURED_UUID = UUID.fromString("ef614301-e72d-441d-b499-53fa29ed787d")

    private var broadcast: LocalBroadcastManager = LocalBroadcastManager.getInstance(applicationContext)
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var secureAcceptThread: AcceptThread? = null
    private var connectThread: ConnectThread? = null
    private var connectedThread: ConnectedThread? = null
    private var connectionState: Int = 0
    private var newConnectionState: Int = 0

    companion object {
        const val STATE_NONE = 0
        const val STATE_LISTEN = 1
        const val STATE_CONNECTING = 2
        const val STATE_CONNECTED = 3
    }

    init {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        connectionState = STATE_NONE
        newConnectionState = connectionState
    }

    @Synchronized fun getState(): Int {
        return connectionState
    }

    @Synchronized fun start() {
        clearConnectThread()
        clearConnectedThread()
        if (secureAcceptThread == null) {
            secureAcceptThread = AcceptThread()
            secureAcceptThread?.start()
        }
    }

    @Synchronized fun connect(
        bluetoothDevice: BluetoothDevice?
    ) {
        if (connectionState == STATE_CONNECTING) {
            clearConnectThread()
        }
        clearConnectedThread()

        connectThread = ConnectThread(bluetoothDevice)
        connectThread?.start()
    }

    @Synchronized fun connected(
        socket: BluetoothSocket?,
        device: BluetoothDevice?
    ) {
        stop()
        connectedThread = ConnectedThread(socket)
        connectedThread?.start()
        sendDataBroadcast(BT.DEVICE_NAME, device?.name+" "+device?.address)
    }

    @Synchronized fun stop() {
        clearConnectThread()
        clearConnectedThread()
        clearSecureAcceptThread()
        connectionState = STATE_NONE
    }

    @Synchronized
    private fun clearConnectThread(){
        if (connectThread != null) {
            connectThread?.cancel()
            connectThread = null
        }
    }
    @Synchronized
    private fun clearConnectedThread(){
        if (connectedThread != null) {
            connectedThread?.cancel()
            connectedThread = null
        }
    }
    @Synchronized
    private fun clearSecureAcceptThread(){
        if (secureAcceptThread != null) {
            secureAcceptThread?.cancel()
            secureAcceptThread = null
        }
    }


    fun write(output: ByteArray) {
        var thread: ConnectedThread?
        synchronized(this) {
            if (connectionState != STATE_CONNECTED) return
            thread = connectedThread
        }
        thread?.write(output)
    }

    private fun connectionFailed() {
        sendDataBroadcast(BT.MESSAGE_ERROR, "Unable to connect device")
        connectionState = STATE_NONE
        this@BluetoothService.start()
    }

    private fun connectionLost() {
        sendDataBroadcast(BT.MESSAGE_ERROR, "Device connection was lost")
        connectionState = STATE_NONE
        this@BluetoothService.start()
    }

    private fun sendDataBroadcast(messageType: String, message:String){
        val intent = Intent(BT.RECEIVER_TOPIC)
        val bundle = Bundle()
        bundle.putString(BT.MESSAGE_TYPE, messageType)
        bundle.putString(BT.MESSAGE, message)
        intent.putExtras(bundle)
        broadcast.sendBroadcast(intent)
    }

    private inner class AcceptThread: Thread() {
        private var bluetoothServerSocket: BluetoothServerSocket? = null
        init {
            try {
                bluetoothServerSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord(
                    SECURED_NAME,
                    SECURED_UUID
                )
                connectionState = STATE_LISTEN

            } catch (e: IOException) {
                Log.e(TAG, "AcceptThread listen() failed", e)
            }
        }

        override fun run() {
            var socket: BluetoothSocket?
            while (connectionState != STATE_CONNECTED) {
                try {
                    socket = bluetoothServerSocket?.accept()
                } catch (e: IOException) {
                    sendDataBroadcast(BT.MESSAGE_ERROR, "Connection accept failed")
                    break
                }
                if (socket != null) {
                    synchronized(this@BluetoothService) {
                        when (connectionState) {
                            STATE_LISTEN, STATE_CONNECTING ->
                                connected(socket, socket.remoteDevice)
                            STATE_NONE, STATE_CONNECTED ->
                                try {
                                    socket.close()
                                } catch (e: IOException) {
                                    Log.e(TAG, "AcceptThread close socket failed", e)
                                }
                            else -> {}
                        }
                    }
                }
            }
        }

        fun cancel() {
            try {
                bluetoothServerSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "AcceptThread close socket failed", e)
            }

        }
    }

    private inner class ConnectThread(private val bluetoothDevice: BluetoothDevice?) : Thread() {
        private val bluetoothSocket: BluetoothSocket?
        init {
            var tempSocket: BluetoothSocket? = null
            try {
                tempSocket = bluetoothDevice?.createRfcommSocketToServiceRecord(SECURED_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "ConnectThread create service socket failed ", e)
            }
            bluetoothSocket = tempSocket
            connectionState = STATE_CONNECTING
        }

        override fun run() {

            try {
                bluetoothSocket?.connect()
            } catch (e: IOException) {
                try {
                    bluetoothSocket?.close()
                } catch (e: IOException) {
                    Log.e(TAG, "ConnectThread close socket failed", e)
                }
                connectionFailed()
                return
            }
            synchronized(this@BluetoothService) {
                connectThread = null
            }
            connected(bluetoothSocket, bluetoothDevice)
        }

        fun cancel() {
            try {
                bluetoothSocket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "ConnectThread close socket failed", e)
            }
        }
    }

    private inner class ConnectedThread(private val socket: BluetoothSocket?) : Thread() {

        private val inputStream: InputStream?
        private val outputStream: OutputStream?

        init {
            var inputFromSocket: InputStream? = null
            var outputFromSocket: OutputStream? = null

            try {
                inputFromSocket = socket?.inputStream
                outputFromSocket = socket?.outputStream
                connectionState = STATE_CONNECTED

            } catch (e: IOException) {
                connectionState = STATE_NONE
            }
            inputStream = inputFromSocket
            outputStream = outputFromSocket
        }

        override fun run() {
            val buffer = ByteArray(1024)
            var bytes: Int

            while (connectionState == STATE_CONNECTED) {
                try {
                    bytes = inputStream?.read(buffer) ?: 0
                    val readMessage = String(buffer, 0, bytes)
                    sendDataBroadcast(BT.DATA_RECEIVED, readMessage)
                } catch (e: IOException) {
                    connectionLost()
                    break
                }
            }
        }

        fun write(buffer: ByteArray) {
            try {
                outputStream?.write(buffer)
            } catch (e: IOException) {
                sendDataBroadcast(BT.MESSAGE_ERROR, "Write failed")
            }
        }

        fun cancel() {
            try {
                socket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "ConnectedThread close socket failed", e)
            }
        }
    }
}