package me.forrest.commonlib.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import me.forrest.commonlib.jh.BTProtocol;
import me.forrest.commonlib.jh.BTProtocolD58B;

public class BLEUtil {

    private final static String TAG = "BLEUtil";
    private final static int REQUEST_ENABLE_BT = 123;
    private boolean isInited;
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private static final long SCAN_PERIOD = 10000; // Stops scanning after 10 seconds.
    private boolean mScanning;
    private boolean mIsConnecting; //标记是否正在连接，知道获取到所有服务
    private int connectionState = STATE_DISCONNECTED;
    private String leftDevType  = "";
    private String rightDevType = "";
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
    private final ArrayList<BluetoothDevice> mBluetoothDevices = new ArrayList<>(5);
    private final HashMap<String, BluetoothGatt> mBluetoothGattMap = new HashMap<>(5);

    private final HashMap<String, UUID> mBluetoothServiceUUIDMap = new HashMap<>(3);
    private final HashMap<String, UUID> mBluetoothNotifyUUIDMap = new HashMap<>(3);
    private final HashMap<String, UUID> mBluetoothWriteUUIDMap = new HashMap<>(3);

    private final HashMap<String, UUID> mBluetoothBatUUIDMap = new HashMap<>(3); // 电池描述符
    private final HashMap<String, BluetoothGattCharacteristic> mBluetoothBatCharMap = new HashMap<>(3); // 电池描述符

    public static final String DEV_TYPE_351  = "JH Remote";
    public static final String DEV_TYPE_D58B = "D58B";
    public static final String DEV_TYPE_W3   = "G.SoundBuds";

    // --------- W3 自定义服务UUID ---------
    public static final String ServiceUUID                         = "ae30";
    public static final String WriteCharacteristicUUID             = "ae01";
    public static final String NotifyCharacteristicUUID            = "ae02";

    public static final String BatteryServiceUUID                  = "180f";       // 0000180f-0000-1000-8000-00805f9b34fb
    public static final String BatteryCharacteristicUUID           = "2a19";       // 00002a19-0000-1000-8000-00805f9b34fb
    public static final String BatteryDescriptor                   = "2902";       // 00002902-0000-1000-8000-00805f9b34fb

    // --------- D58B Service_UUID Characteristic_UUID ---------
    public static final String D58B_Dev_Info_Service_UUID          = "180a";  // Android uuid均为小写字母
    public static final String D58B_Manufacturer_Char_UUID         = "2a29";
    public static final String D58B_Model_Name_Char_UUID           = "2a24";
    public static final String D58B_HW_Revision_Char_UUID          = "2a27";
    public static final String D58B_FW_Revision_Char_UUID          = "2a26";

    public static final String D58B_User_Interface_Service_UUID    = "1860";
    public static final String D58B_Volume_Level_Char_UUID         = "2a90";
    public static final String D58B_Program_Char_UUID              = "2a91"; //模式
    public static final String D58B_Device_Settings_Char_UUID      = "2a92";
    public static final String D58B_Battery_Level_Char_UUID        = "2a93";
    public static final String D58B_Cmd_Interface_Char_UUID        = "2a94";

    public static final String D58B_RPS_Service_UUID               = "1861";
    public static final String D58B_RPS_Read_Char_UUID             = "2ab0";
    public static final String D58B_RPS_Write_Char_UUID            = "2ab1";
    public static final String D58B_RPS_Orientation_Char_UUID      = "2ab2";
    public static final String D58B_RPS_Unlock_UUID                = "2ab5";


    public static final int STATE_DISCONNECTED                     = 0;
    public static final int STATE_CONNECTING                       = 1;
    public static final int STATE_CONNECTED                        = 2;
    public static final int STATE_DISCONNECTING                    = 3;
    public static final int STATE_GET_GATT_SERVICES_OVER           = 4; // 获取服务完成
    public static final int STATE_RECONNECTING                     = 5; // 正在自动重连

    public final static String ACTION_GATT_CONNECTED               = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED            = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED     = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE               = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA                          = "com.example.bluetooth.le.EXTRA_DATA";

    private final Object mStateLock = new Object();
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private final ArrayList<JHBLEListener> mBleListeners = new ArrayList<>(5);
    private final ArrayList<BLEDevice> mBleDevices = new ArrayList<>(5);

    private static BLEUtil instance;

    public static class BLEDevice {
        public String  deviceName;
        public String  mac;
        public int     connectStatus;
        public String  devType;
        public boolean needAutoReconnect; // 需要自动重连
        public String  alias;             // 别名

        public BLEDevice() {

        }

        public BLEDevice(String deviceName, String mac, int connectStatus, String devType) {
            this.deviceName    = deviceName;
            this.mac           = mac;
            this.connectStatus = connectStatus;
            this.devType       = devType;
        }

        @Override
        public String toString() {
            return "BLEDevice{" +
                    "deviceName='" + deviceName + '\'' +
                    ", mac='" + mac + '\'' +
                    ", connectStatus=" + connectStatus +
                    ", devType=" + devType +
                    ", needAutoReconnect=" + needAutoReconnect +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BLEDevice bleDevice = (BLEDevice) o;
            return connectStatus == bleDevice.connectStatus &&
                    Objects.equals(deviceName, bleDevice.deviceName) &&
                    Objects.equals(mac, bleDevice.mac);
        }

        @Override
        public int hashCode() {
            return Objects.hash(deviceName, mac, connectStatus);
        }
    }

    public interface JHBLEListener {
        void updateBLEDevice(List<BLEDevice> bleDevices);
        void onBatteryChanged(String deviceName, int value);
        void onReadChanged(String deviceName, byte[] values);
//        void updateReconnectBLEDevice();
    }

    public void addJHBleListener(JHBLEListener listener) {
        mBleListeners.add(listener);
    }

    public void removeJHBLEListener(JHBLEListener listener) {
        for (JHBLEListener l : mBleListeners) {
            if(l == listener) {
                mBleListeners.remove(l);
                break;
            }
        }
    }

    public synchronized void updateListenerForBLEDevices() {
        for (JHBLEListener l : mBleListeners) {
            l.updateBLEDevice(mBleDevices);
        }
    }

    public synchronized void updateListenerForBattery(String deviceName, int value) {
        for (JHBLEListener l : mBleListeners) {
            l.onBatteryChanged(deviceName, value);
        }
    }

    public synchronized void updateListenerForRead(String deviceName, byte[] values) {
        for (JHBLEListener l : mBleListeners) {
            l.onReadChanged(deviceName, values);
        }
    }

    public synchronized void removeBluetoothDevice(String mac) {
        for(BluetoothDevice device: mBluetoothDevices) {
            if (mac.equals(device.getAddress())) {
                mBluetoothDevices.remove(device);
                break;
            }
        }
    }

    public List<BLEDevice> getBLEDevices() {
        return this.mBleDevices;
    }

    public static synchronized BLEUtil getInstance() {
        if (instance == null) {
            instance = new BLEUtil();
        }
        return instance;
    }

    private BLEUtil() {}

    public void init(Context context) {
        if (isInited) return;
        Log.d(TAG, "init");
        this.isInited = true;
        this.context = context;
        mBackgroundThread = new HandlerThread("le_thread");
        mBackgroundThread.start();
        synchronized (mStateLock) {
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }
        // Initializes Bluetooth adapter.
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager != null ? bluetoothManager.getAdapter() : null;
    }

    public void release() {
        Log.d(TAG, "release");
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            synchronized (mStateLock) {
                mBackgroundHandler = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.isInited = false;
    }

    public boolean isEnableBT() {
        if (bluetoothAdapter != null) {
            return bluetoothAdapter.isEnabled();
        }
        return false;
    }

    // 是否正在获取服务，如果是，需要等待一下。
    public boolean isConnecting() {
        return mIsConnecting;
    }

    public boolean isScanning() {
        return mScanning;
    }

    // 获取连接设备的类型
    public String getDevType() {
        if (!this.leftDevType.equals("")) {
            return this.leftDevType;
        }
        if (!this.rightDevType.equals("")) {
            return this.rightDevType;
        }
        return "";
    }

    public void jumpToEnableBT(Activity activity) {
        // Ensures Bluetooth is available on the device and it is enabled.
        // If not, displays a dialog requesting user permission to enable Bluetooth.
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    // 扫描
    public void scanLeDevice(final boolean enable) {
        synchronized (mStateLock) {
            if (bluetoothAdapter == null) return;
            if (!isEnableBT()) return;
            if (bluetoothAdapter.getBluetoothLeScanner() == null) return;
            if (enable && !mScanning) {
                // Stops scanning after a pre-defined scan period.
                mBackgroundHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScanning = false;
                        bluetoothAdapter.getBluetoothLeScanner().stopScan(leScanCallback);
                        Log.d(TAG, "stop scan");
                    }
                }, SCAN_PERIOD);

                mScanning = true;
                bluetoothAdapter.getBluetoothLeScanner().startScan(leScanCallback);
                // 过滤掉没连接成功的设备
                filterConnectedDevices();
                Log.d(TAG, "start scan");

            } else if (!enable) {
                mScanning = false;
                bluetoothAdapter.getBluetoothLeScanner().stopScan(leScanCallback);
                Log.d(TAG, "stop scan");
            }
        }
    }

//    public void connectBLE(BluetoothDevice device) {
//        // 一个 Context 对象、autoConnect（布尔值，指示是否在可用时自动连接到 BLE 设备），以及对 BluetoothGattCallback 的引用
//        if (bluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == BluetoothProfile.STATE_DISCONNECTED) {
//            BluetoothGatt bluetoothGatt = device.connectGatt(this.context, false, gattCallback);
//            mBluetoothGattMap.put(device.getAddress(), bluetoothGatt);
//            Log.d(TAG, "connectBLE");
//        }
//    }

    public void connectBLE(String mac) {
        for (BluetoothDevice device: mBluetoothDevices) {
            if (mac.equals(device.getAddress())) {
//                connectBLE(device);
                // 一个 Context 对象、autoConnect（布尔值，指示是否在可用时自动连接到 BLE 设备），以及对 BluetoothGattCallback 的引用
                if (bluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "connectBLE");
                    // 保存连接的设备型号
                    BLEDevice bleDevice = findBLEDevice(mac);
                    if (bleDevice != null && bleDevice.deviceName.contains("-L")) { this.leftDevType = bleDevice.devType; }
                    else if (bleDevice != null && bleDevice.deviceName.contains("-R")) { this.rightDevType = bleDevice.devType; }

                    // 更新UI状态
                    mIsConnecting = true;
                    updateBLEDeviceConnectStatus(mac, STATE_CONNECTING, true);
                    updateListenerForBLEDevices();
                    BluetoothGatt bluetoothGatt = device.connectGatt(this.context, false, gattCallback);
                    mBluetoothGattMap.put(device.getAddress(), bluetoothGatt);
                }
                break;
            }
        }
    }

    public void reconnectBLE(String mac) {
        for (BluetoothDevice device: mBluetoothDevices) {
            if (mac.equals(device.getAddress())) {
//                connectBLE(device);
                // 一个 Context 对象、autoConnect（布尔值，指示是否在可用时自动连接到 BLE 设备），以及对 BluetoothGattCallback 的引用
                if (bluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "reconnectBLE");
                    // 保存连接的设备型号
                    BLEDevice bleDevice = findBLEDevice(mac);
                    if (bleDevice != null && bleDevice.deviceName.contains("-L")) { this.leftDevType = bleDevice.devType; }
                    else if (bleDevice != null && bleDevice.deviceName.contains("-R")) { this.rightDevType = bleDevice.devType; }

                    // 更新UI状态
                    mIsConnecting = true;
                    updateBLEDeviceConnectStatus(mac, STATE_RECONNECTING, true);
                    updateListenerForBLEDevices();
                    BluetoothGatt bluetoothGatt = device.connectGatt(this.context, false, gattCallback);
                    mBluetoothGattMap.put(device.getAddress(), bluetoothGatt);
                }
                break;
            }
        }
    }

    public void disconnectBLE(String mac) {
        for (BluetoothDevice device: mBluetoothDevices) {
            if (mac.equals(device.getAddress())) {
                BluetoothGatt bluetoothGatt = mBluetoothGattMap.get(mac);
                // 删除连接的设备型号
                BLEDevice bleDevice = findBLEDevice(mac);
                if (bleDevice != null && bleDevice.deviceName.contains("-L")) { this.leftDevType = ""; }
                else if (bleDevice != null && bleDevice.deviceName.contains("-R")) { this.rightDevType = ""; }

                if (bluetoothGatt != null && bluetoothManager.getConnectionState(device, BluetoothProfile.GATT) != BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "disconnectBLE 等待更新断开状态");
                    updateBLEDeviceConnectStatus(mac, STATE_DISCONNECTING, false);
                    bluetoothGatt.disconnect();
                } else if (bluetoothGatt != null && bluetoothManager.getConnectionState(device, BluetoothProfile.GATT) == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d(TAG, "disconnectBLE 主动更新断开状态");
                    updateBLEDeviceConnectStatus(mac, STATE_DISCONNECTED, false);
                    updateListenerForBLEDevices();
                    bluetoothGatt.disconnect();
                }
                break;
            }
        }
    }

    public int getConnectStatus(BluetoothDevice device) {
        return bluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
    }

    public int getConnectStatus(String mac) {
        for (BluetoothDevice device : mBluetoothDevices) {
            if (device.getAddress().equals(mac)) {
                return bluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
            }
        }
        return BluetoothProfile.STATE_DISCONNECTED;
    }

    // 过滤连接上了的设备，并更新UI
    private void filterConnectedDevices() {
        Iterator<BluetoothDevice> it = mBluetoothDevices.iterator();
        while (it.hasNext()){
            BluetoothDevice device = it.next();
            if (bluetoothManager.getConnectionState(device, BluetoothProfile.GATT) != BluetoothProfile.STATE_CONNECTED) {
                it.remove();
            }
        }

        Iterator<BLEDevice> it2 = mBleDevices.iterator();
        while (it2.hasNext()){
            BLEDevice device = it2.next();

            // 断开正在连接的设备
            if (device.connectStatus == STATE_CONNECTING || device.connectStatus == STATE_RECONNECTING) {
                BluetoothGatt bluetoothGatt = mBluetoothGattMap.get(device.mac);
                if (bluetoothGatt != null) {
                    bluetoothGatt.disconnect();
                }
            }

            if (device.connectStatus != STATE_CONNECTED && device.connectStatus != STATE_GET_GATT_SERVICES_OVER) {
                it2.remove();
            }
        }
        updateListenerForBLEDevices();
    }

    // 更新BLEDevice的连接状态，通知UI更新
    private void updateBLEDeviceConnectStatus(String mac, int connectionState) {
        for (BLEDevice device : mBleDevices) {
            if (mac.equals(device.mac)) {
                device.connectStatus = connectionState;
            }
        }
    }

    private void updateBLEDeviceConnectStatus(String mac, int connectionState, boolean needAutoReconnect) {
        for (BLEDevice device : mBleDevices) {
            if (mac.equals(device.mac)) {
                device.connectStatus = connectionState;
                device.needAutoReconnect = needAutoReconnect;
            }
        }
    }

    private BLEDevice findBLEDevice(String mac) {
        for (BLEDevice device : mBleDevices) {
            if (mac.equals(device.mac)) {
                return device;
            }
        }
        return null;
    }

    // CB: 扫描回调接口
    private final ScanCallback leScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            // callbackType = 1
            // result = ScanResult {
            // device=AC:01:37:C9:4F:80,
            // scanRecord=ScanRecord [mAdvertiseFlags=6, mServiceUuids=[0000af30-0000-1000-8000-00805f9b34fb],
            // mManufacturerSpecificData={65278=[-84, 1, 55, -55, 79, -128]}, // result.getScanRecord().getManufacturerSpecificData().keyAt(0)) == 0xFEFE
            // mServiceData={},
            // mTxPowerLevel=-2147483648,
            // mDeviceName=G.SoundBuds-R],
            // rssi=-49,
            // timestampNanos=243543929569232,
            // eventType=27,
            // primaryPhy=1,
            // secondaryPhy=0,
            // advertisingSid=255,
            // txPower=127,
            // periodicAdvertisingInterval=0 }
            BluetoothDevice device = result.getDevice();
            String name = device.getName();
            if ( name != null && (name.endsWith("-L") || name.endsWith("-R")) ) {
                Log.d(TAG, "++++++++++++++++++++++++++++++++++++++++++++++++");
                String mac = device.getAddress();
                int status = bluetoothManager.getConnectionState(device, BluetoothProfile.GATT);
                Log.d(TAG, "callbackType  = "+callbackType + "\nresult = " + result);
                Log.d(TAG, "connectStatus = " + status);
                Log.d(TAG, "mac           = " + device.getAddress());
                Log.d(TAG, "------------------------------------------------"); Log.d(TAG, "");
                // 去掉被重复扫描到的设备
                for (BluetoothDevice d : mBluetoothDevices) {
                   if (d.getAddress().equals(mac)) {
                       return;
                   }
                }

                List<ParcelUuid> uuids = result.getScanRecord().getServiceUuids();
                String uuid;
                String devType = DEV_TYPE_W3;
                if (uuids.size() > 0) {
                    uuid = uuids.get(0).getUuid().toString();
                    if (uuid.contains("af30")) {
                        devType = DEV_TYPE_W3;
                    } else if (uuid.contains("180a")) {
                        devType = DEV_TYPE_D58B;
                    }
                }
                mBluetoothDevices.add(device);
                BLEDevice bleDevice = new BLEDevice(device.getName(), device.getAddress(), status, devType);
                mBleDevices.add(bleDevice);
                updateListenerForBLEDevices();
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                Log.d(TAG, "* result = " + result + "\n");
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.d(TAG, "errorCode = " + errorCode);
        }
    };

    // CB: BLE 状态回调
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.d(TAG, "gattCallback onConnectionStateChange" + " newState:" + newState + " gatt = " + gatt);
                String intentAction;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "STATE_CONNECTED to GATT server.");
                    connectionState = STATE_CONNECTED;
//                    boolean result = gatt.discoverServices();
//                    Log.i(TAG, "Attempting to start service discovery: " + result);
                    updateBLEDeviceConnectStatus(gatt.getDevice().getAddress(), STATE_CONNECTED);
                    updateListenerForBLEDevices();
//                    BluetoothGatt bluetoothGatt = mBluetoothGattMap.get(gatt.getDevice().getAddress());
                    gatt.requestMtu(32); // 加大传输的最大字节数

                } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                    Log.i(TAG, "STATE_CONNECTING to GATT server.");
                    connectionState = STATE_CONNECTING;
                    updateBLEDeviceConnectStatus(gatt.getDevice().getAddress(), STATE_CONNECTING);
                    updateListenerForBLEDevices();

                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    Log.i(TAG, "STATE_DISCONNECTING from GATT server.");
                    connectionState = STATE_DISCONNECTING;
                    updateBLEDeviceConnectStatus(gatt.getDevice().getAddress(), STATE_DISCONNECTING);
                    updateListenerForBLEDevices();

                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "STATE_DISCONNECTED from GATT server. ");
                    String mac = gatt.getDevice().getAddress();
                    connectionState = STATE_DISCONNECTED;
                    updateBLEDeviceConnectStatus(gatt.getDevice().getAddress(), STATE_DISCONNECTED);
                    updateListenerForBLEDevices();
                    gatt.close();

                    if (gatt.getDevice().getName().contains("-L")) {
                        leftDevType = "";
                    } else if (gatt.getDevice().getName().contains("-R")){
                        rightDevType = "";
                    }

                    // 自动重连BLE设备
                    BLEDevice bleDevice = findBLEDevice(mac);
                    if (bleDevice != null && bleDevice.needAutoReconnect) {
                        reconnectBLE(mac);
                    }
                }
            }

            // 发现了服务
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.d(TAG, "gattCallback onServicesDiscovered " + "status:"+status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // 显示服务支持的特征
                    displayGattServices(gatt.getDevice().getAddress(), gatt.getServices());
                }
            }

            // Result of a characteristic read operation
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.d(TAG, "gattCallback 读回调"
                        + " characteristic = " + characteristic.getUuid().toString()
                        + " deviceName = " + gatt.getDevice().getName()
                        + " status = " + status
                        + " value = " + characteristic.getValue()[0]);
                String uuid = characteristic.getUuid().toString();
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (uuid.contains(BatteryCharacteristicUUID)) {
                        updateListenerForBattery(gatt.getDevice().getName(), characteristic.getValue()[0]);

                    // D58B 专用特征uuid
                    } else if (uuid.contains(D58B_Volume_Level_Char_UUID) || uuid.contains(D58B_Program_Char_UUID) || uuid.contains(D58B_RPS_Read_Char_UUID)) {
                        BTProtocolD58B.share.checkAndPraseFeedback(uuid, gatt.getDevice().getName(), characteristic.getValue(), BTProtocol.Read_Success);

                    } else if (uuid.contains(D58B_Battery_Level_Char_UUID)) {
                        updateListenerForBattery(gatt.getDevice().getName(), characteristic.getValue()[0]);
                    }
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.d(TAG, "gattCallback 写回调"
                        + " characteristic = " + characteristic.getUuid().toString()
                        + " deviceName = " + gatt.getDevice().getName()
                        + " status =  " + status
                        + " value = 0x" + NumberUtil.byteArraytoHex(characteristic.getValue(), characteristic.getValue().length, " "));
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                }
                String uuid = characteristic.getUuid().toString();
                if (uuid.contains(D58B_Volume_Level_Char_UUID) || uuid.contains(D58B_Program_Char_UUID) || uuid.contains(D58B_RPS_Read_Char_UUID)) {
                    BTProtocolD58B.share.checkAndPraseFeedback(uuid, gatt.getDevice().getName(), characteristic.getValue(), BTProtocol.Write_Success);
                }
            }

            // Characteristic notification
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                Log.d(TAG, "gattCallback 通知回调"
                        + " characteristic = " + characteristic.getUuid().toString()
                        + " deviceName = " + gatt.getDevice().getName()
                        + " value = 0x" + NumberUtil.byteArraytoHex(characteristic.getValue(), characteristic.getValue().length, " "));

                String uuid = characteristic.getUuid().toString();
                if (uuid.contains(BatteryCharacteristicUUID)) {
                    updateListenerForBattery(gatt.getDevice().getName(), characteristic.getValue()[0]);

                } else if (characteristic.getUuid().toString().contains(NotifyCharacteristicUUID)) {
                    BTProtocol.share.checkAndParseFeedback(gatt.getDevice().getName(), characteristic.getValue());

                // D58B 专用特征uuid
                } else if (uuid.contains(D58B_Volume_Level_Char_UUID) || uuid.contains(D58B_Program_Char_UUID) || uuid.contains(D58B_RPS_Read_Char_UUID)) {
                    BTProtocolD58B.share.checkAndPraseFeedback(uuid, gatt.getDevice().getName(), characteristic.getValue(), BTProtocol.Report_Success);

                } else if (uuid.contains(D58B_Battery_Level_Char_UUID)) {
                    updateListenerForBattery(gatt.getDevice().getName(), characteristic.getValue()[0]);
                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                Log.d(TAG, "gattCallback onDescriptorWrite " + " status = " + status);
            }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.d(TAG, "gattCallback onMtuChanged " + "status:"+status + " mtu = " + mtu);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mIsConnecting = true;
                boolean result = gatt.discoverServices();
                Log.d(TAG, "Attempting to start service discovery: " + result);
            }
        }
    };


    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        context.sendBroadcast(intent);
    }

    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView on the UI.
    private void displayGattServices(String mac, List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        mIsConnecting = true;
        String uuid = null;
        mGattCharacteristics = new ArrayList<>();

        long delay = 0;

        // Loops through available GATT Services. 遍历各种服务,找到需要的服务UUID
        for (BluetoothGattService gattService : gattServices) {
            uuid = gattService.getUuid().toString();
            Log.d(TAG, "============================================================");
            Log.d(TAG, "Service = " + uuid);

            if (uuid.contains(ServiceUUID)) {
                mBluetoothServiceUUIDMap.put(mac, gattService.getUuid());
                // 找到自定义服务的特征UUID
                for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                    uuid = gattCharacteristic.getUuid().toString();
                    Log.d(TAG, "    Characteristic = " + uuid);
                    if (uuid.contains(WriteCharacteristicUUID)) {
                        mBluetoothWriteUUIDMap.put(mac, gattCharacteristic.getUuid());

                    } else if (uuid.contains(NotifyCharacteristicUUID)) {
                        List<BluetoothGattDescriptor> descriptors = gattCharacteristic.getDescriptors();
                        for (BluetoothGattDescriptor descriptor : descriptors) {
                            Log.d(TAG, "        descriptor = " + descriptor.getUuid().toString());
                        }

                        mBluetoothNotifyUUIDMap.put(mac, gattCharacteristic.getUuid());
                        // 设置通知
                        BluetoothGatt gatt = mBluetoothGattMap.get(mac);
                        BluetoothGattDescriptor descriptor = descriptors.get(0);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        if (gatt != null && gatt.writeDescriptor(descriptor)) {
                            gatt.setCharacteristicNotification(gattCharacteristic, true);
                        }
                    }
                }

            } else if (uuid.contains(BatteryServiceUUID)) {
                for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                    uuid = gattCharacteristic.getUuid().toString();
                    Log.d(TAG, "    Characteristic = " + uuid);
                    if (uuid.contains(BatteryCharacteristicUUID)) { // 监听电池通知
                        List<BluetoothGattDescriptor> descriptors = gattCharacteristic.getDescriptors();
                        for (BluetoothGattDescriptor descriptor : descriptors) {
                            Log.d(TAG, "        descriptor = " + descriptor.getUuid().toString());
                        }
                        // 获取属性 0x12 PROPERTY_READ | PROPERTY_NOTIFY
                        //int properties = gattCharacteristic.getProperties();

                        // 设置通知 与上面的通知同时设置有问题。延时设置一下。
                        mBackgroundHandler.postDelayed(() -> {
                            BluetoothGatt gatt = mBluetoothGattMap.get(mac);
                            // 读一下电池电量
//                        if (gatt != null) { gatt.readCharacteristic(gattCharacteristic); }
                            BluetoothGattDescriptor descriptor = descriptors.get(0);
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            if (gatt != null && gatt.writeDescriptor(descriptor)) {
                                gatt.setCharacteristicNotification(gattCharacteristic, true);
                            }
                        }, 1000);
                        // 记录电池 read描述符
                        mBluetoothBatCharMap.put(mac, gattCharacteristic);
                    }
                }

            // D58B 各种服务
            } else if (uuid.contains(D58B_Dev_Info_Service_UUID)
                    || uuid.contains(D58B_User_Interface_Service_UUID)
                    ||uuid.contains(D58B_RPS_Service_UUID)) {

                // 遍历每种服务的Characteristic
                for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                    uuid = gattCharacteristic.getUuid().toString();
                    Log.d(TAG, "    Characteristic = " + uuid);
                    if (uuid.contains(D58B_Volume_Level_Char_UUID) || uuid.contains(D58B_Program_Char_UUID)
                        || uuid.contains(D58B_Battery_Level_Char_UUID) || uuid.contains(D58B_RPS_Read_Char_UUID) ) {
                        List<BluetoothGattDescriptor> descriptors = gattCharacteristic.getDescriptors();
                        for(BluetoothGattDescriptor descriptor : descriptors) {
                            Log.d(TAG, "        descriptor = " + descriptor.getUuid().toString());
                        }

                        // 设置通知, 需要设置间隔，否则只有第一个有效
                        mBackgroundHandler.postDelayed(() -> {
                            BluetoothGatt gatt = mBluetoothGattMap.get(mac);
                            BluetoothGattDescriptor descriptor = descriptors.get(0);

                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            Log.d(TAG, "setCharacteristicNotification ++++++ : " + gattCharacteristic.getUuid().toString());
                            if (gatt != null && gatt.writeDescriptor(descriptor)) {
                                Log.d(TAG, "setCharacteristicNotification ------ : " + gattCharacteristic.getUuid().toString());
                                gatt.setCharacteristicNotification(gattCharacteristic, true);
                            }
                        }, delay);
                        delay = delay + 150; // 测试发现2ab0这个通知，如果间隔太短，可能设置通知不成功
                    }
                }
            }
            Log.d(TAG, "============================================================"); Log.d(TAG, "");
        }
        updateBLEDeviceConnectStatus(mac, STATE_GET_GATT_SERVICES_OVER);
        updateListenerForBLEDevices();
        mIsConnecting = false;
    }

    public boolean writeCharacteristic(String mac, byte[] data) {
        //check mBluetoothGatt is available
        BluetoothGatt gatt = mBluetoothGattMap.get(mac);
        if (gatt == null || getConnectStatus(mac) != BluetoothProfile.STATE_CONNECTED) {
            Log.e(TAG, "### writeCharacteristic not available!" + mac);
            return false;
        }
        BluetoothGattService service = gatt.getService(mBluetoothServiceUUIDMap.get(mac));
        if (service == null) {
            Log.e(TAG, "### writeCharacteristic service not found! " + mac);
            return false;
        }
        BluetoothGattCharacteristic chat = service.getCharacteristic(mBluetoothWriteUUIDMap.get(mac));
        if (chat == null) {
            Log.e(TAG,  "### writeCharacteristic char not found!" + mac);
            return false;
        }
        chat.setValue(data);
        boolean result = gatt.writeCharacteristic(chat);
        Log.d(TAG, "writeCharacteristic " + mac + " result = " + result);
        return result;
    }

//    public boolean readCharacteristic(String mac, byte[] data) {
//
//    }

    // 读电量
    public boolean readBatValue(String mac) {
        //check mBluetoothGatt is available
        BluetoothGatt gatt = mBluetoothGattMap.get(mac);
        if (gatt == null || getConnectStatus(mac) != BluetoothProfile.STATE_CONNECTED) {
            Log.e(TAG, "### readBatValue not available!" + mac);
            return false;
        }
        BluetoothGattCharacteristic chat = mBluetoothBatCharMap.get(mac);
        if (chat == null) {
            Log.e(TAG,  "### readBatValue char not found!" + mac);
            return false;
        }
        boolean result = gatt.readCharacteristic(chat);
        Log.d(TAG, "readBatValue " + mac + " result = " + result);
        return result;
    }

    public boolean writeCharacteristic(String mac, String serviceUUID, String chatUUID, byte[] data) {
        //check mBluetoothGatt is available
        BluetoothGatt gatt = mBluetoothGattMap.get(mac);
        if (gatt == null || getConnectStatus(mac) != BluetoothProfile.STATE_CONNECTED) {
            Log.e(TAG, "### 写数据 not available!" + mac);
            return false;
        }
        List<BluetoothGattService> services = gatt.getServices();
        BluetoothGattService service = null;
        for (BluetoothGattService serv : services) {
            if (serv.getUuid().toString().contains(serviceUUID)) {
                service = serv;
                break;
            }
        }
        if (service == null) {
            Log.e(TAG, "### 写数据 service not found! " + mac);
            return false;
        }

        BluetoothGattCharacteristic chat = null;
        List<BluetoothGattCharacteristic> chats = service.getCharacteristics();
        for (BluetoothGattCharacteristic _chat : chats) {
            if (_chat.getUuid().toString().contains(chatUUID)) {
                chat = _chat;
                break;
            }
        }
        if (chat == null) {
            Log.e(TAG,  "### 写数据 char not found!" + mac);
            return false;
        }
        chat.setValue(data);
        boolean result = gatt.writeCharacteristic(chat);
        Log.d(TAG, "写数据 " + mac + " " + chatUUID + " result = " + result);
        return result;
    }

    public boolean readCharacteristic(String mac, String serviceUUID, String chatUUID) {
        //check mBluetoothGatt is available
        BluetoothGatt gatt = mBluetoothGattMap.get(mac);
        if (gatt == null || getConnectStatus(mac) != BluetoothProfile.STATE_CONNECTED) {
            Log.e(TAG, "### readCharacteristic not available!" + mac);
            return false;
        }

        List<BluetoothGattService> services = gatt.getServices();
        BluetoothGattService service = null;
        for (BluetoothGattService serv : services) {
            if (serv.getUuid().toString().contains(serviceUUID)) {
                service = serv;
                break;
            }
        }
        if (service == null) {
            Log.e(TAG, "### 读数据 service not found! " + mac);
            return false;
        }

        BluetoothGattCharacteristic chat = null;
        List<BluetoothGattCharacteristic> chats = service.getCharacteristics();
        for (BluetoothGattCharacteristic _chat : chats) {
            if (_chat.getUuid().toString().contains(chatUUID)) {
                chat = _chat;
                break;
            }
        }
        if (chat == null) {
            Log.e(TAG,  "### 读数据 chat not found!" + mac);
            return false;
        }
        boolean result = gatt.readCharacteristic(chat);
        Log.d(TAG, "读数据 " + mac + " " + chatUUID + " result = " + result);
        return result;
    }


}
