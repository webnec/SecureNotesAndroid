package pl.webnec.securenotes.fragments

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.google.gson.Gson
import pl.webnec.securenotes.R
import pl.webnec.securenotes.adapters.DevicesRecyclerViewAdapter
import pl.webnec.securenotes.base.BaseFragment
import pl.webnec.securenotes.base.RecyclerViewClickListener
import pl.webnec.securenotes.databinding.FragmentShareViaBluetoothBinding
import pl.webnec.securenotes.models.DeviceData
import pl.webnec.securenotes.services.BluetoothService
import pl.webnec.securenotes.utilities.BT
import pl.webnec.securenotes.utilities.TAG
import pl.webnec.securenotes.viewmodels.MainViewModel
import java.lang.Exception


class ShareViaBluetoothFragment : BaseFragment<MainViewModel, FragmentShareViaBluetoothBinding>(),
    RecyclerViewClickListener {

    private val REQUEST_ENABLE_BT = 123
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var isConnected: Boolean = false
    private var bluetoothService: BluetoothService? = null
    private var currentConnectedDevice: DeviceData? = null
    private var stringBuilder: StringBuilder = java.lang.StringBuilder()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(requireActivity(), object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_shareViaBluetoothFragment_to_notesFragment)
            }
        })

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_shareViaBluetoothFragment_to_notesFragment)
        }

        binding.buttonSendDataToConnectedDevice.setOnClickListener {
            sendMessage(viewModel.getSecretMessagesInJSON())
        }
        initBluetooth()
    }

    private fun initBluetooth(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothService = BluetoothService(requireContext().applicationContext)

        if (bluetoothAdapter == null)
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.not_compatible))
                .setMessage(getString(R.string.no_support))
                .setPositiveButton("OK") { _, _ ->
                    findNavController().navigate(R.id.action_shareViaBluetoothFragment_to_notesFragment)
                }
                .show()

        else {
            if (bluetoothAdapter?.isEnabled == false) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT)
            }
            setPairedDevicesToRecyclerView()
        }
    }

    fun setPairedDevicesToRecyclerView(){
        val pairedDevices = bluetoothAdapter?.bondedDevices
        val devicesList = arrayListOf<DeviceData>()

        if (pairedDevices?.size ?: 0 > 0) {
            for (device in pairedDevices!!) {
                val deviceName = device.name
                val deviceHardwareAddress = device.address
                devicesList.add(DeviceData(deviceName,deviceHardwareAddress))
            }
            val adapter = DevicesRecyclerViewAdapter(devicesList, this)
            binding.recyclerViewDevices.also { recycler ->
                recycler.layoutManager = LinearLayoutManager(requireContext())
                recycler.setHasFixedSize(true)
                (recycler.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
                recycler.adapter = adapter
            }
        } else {
            binding.textViewPairedDevicesInformation.text = getString(R.string.no_paired_devices)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            initBluetooth()
        }
    }

    private fun setConnectionStatus(status: String){
        binding.textViewStatus.text = status

    }

    private val bluetoothServiceReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try{
                val bundle = intent.extras;
                if(bundle != null && !bundle.isEmpty){
                    val typeOfMessage = bundle.getString(BT.MESSAGE_TYPE)!!
                    val message = bundle.getString(BT.MESSAGE)!!
                    bluetoothServiceReceiverMessagesResolver(typeOfMessage, message)
                }
            } catch (e: Exception){
                Log.e(TAG, "EXCEPTION: messageReceiver.onReceive: $e")
            }
        }
    }


    private fun bluetoothServiceReceiverMessagesResolver(type: String, message: String) {
        when(type){
            BT.DEVICE_NAME -> {
                setConnectionStatus(true, message)
            }
            BT.MESSAGE_ERROR -> {
                setConnectionStatus(false, null)
                errorInfo("Connection error")
            }
            BT.DATA_RECEIVED -> {
                if(message.startsWith("[")){
                    stringBuilder.clear()
                }
                stringBuilder.append(message)
                if(message.endsWith("]")){
                    receivedDataDialog(stringBuilder.toString())
                }
            }
        }
    }

    private fun setConnectionStatus(isConnected: Boolean, device: String?){
        this.isConnected = isConnected
        binding.isConnected = isConnected
        if(isConnected){
            binding.textViewStatus.text = "Connected to $device"
        } else {
            binding.textViewStatus.text = "Not connected"
            currentConnectedDevice = null
        }

    }

    private fun connectDevice(deviceData: DeviceData) {
        val deviceAddress = deviceData.deviceHardwareAddress
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)
        setConnectionStatus(getString(R.string.connecting))
        bluetoothService?.connect(device)
        currentConnectedDevice = deviceData
    }

    private fun sendMessage(message: String) {
        if(isConnected){
            if (message.isNotEmpty()) {
                val send = message.toByteArray()
                bluetoothService?.write(send)
            }
        }
    }

    private fun receivedDataDialog(data: String){
        try {
            stringBuilder.clear()
            val listOfMessages = Gson().fromJson<List<String>>(data, List::class.java)
            val message: String = getString(R.string.received)+
                    " "+listOfMessages.size+
                    " "+getString(R.string.messages)+
                    ".\n"+getString(R.string.what_you_want_to_do_with_data)
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.received_data))
                .setMessage(message)
                .setPositiveButton(R.string.add_to_exist) { _, _ ->
                    listOfMessages.forEach { message -> viewModel.addMessage(message)}
                }
                .setNegativeButton(R.string.override) { _, _ ->
                    viewModel.clearViewModelHoldingData()
                    listOfMessages.forEach { message -> viewModel.addMessage(message)}
                }
                .setNeutralButton(R.string.cancel) { _, _ -> 0}
                .show()
        } catch (e: Exception){
            Log.e(TAG, "receivedDataDialog: Exception ", e)
        }
    }

    override fun onRecyclerViewItemClick(view: View, deviceData: Any) {
        if(deviceData is DeviceData){
            connectDevice(deviceData)
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager
            .getInstance(requireContext().applicationContext)
            .registerReceiver(
                bluetoothServiceReceiver,
                IntentFilter(BT.RECEIVER_TOPIC)
            )
    }

    override fun onStop() {
        super.onStop()
        bluetoothService?.stop()
        LocalBroadcastManager.getInstance(requireContext().applicationContext).unregisterReceiver(bluetoothServiceReceiver)
    }
    override fun onResume() {
        super.onResume()
        if (bluetoothService != null) {
            if (bluetoothService?.getState() == BluetoothService.STATE_NONE) {
                bluetoothService?.start()
            }
        }
    }

    override fun getViewModel() = MainViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentShareViaBluetoothBinding.inflate(inflater, container, false)
}