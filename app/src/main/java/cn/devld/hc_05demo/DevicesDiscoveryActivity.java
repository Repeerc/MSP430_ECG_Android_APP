package cn.devld.hc_05demo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/** 搜索新设备
 * Created by Administrator on 2016/9/21 0021.
 */
public class DevicesDiscoveryActivity extends AppCompatActivity implements OnItemClickListener {

    public static final String EXTRA_DEVICE = "FOUNT_DEVICE_OK";
    private static final int R_ENABLE_BT = 0xad;

    private RecyclerView mRecyclerView;
    private DevicesListAdapter mDevicesListAdapter;
    private LinearLayoutManager mLinearLayoutMgr;

    private BluetoothAdapter mBluetoothAdapter;

    private BroadcastReceiver mDeviceFoundReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRecyclerView = new RecyclerView(this);
        setContentView(mRecyclerView);
        mDevicesListAdapter = new DevicesListAdapter(this);
        mDevicesListAdapter.setOnItemListener(this);
        mLinearLayoutMgr = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutMgr);
        mRecyclerView.setAdapter(mDevicesListAdapter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.toast_no_bt, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, R_ENABLE_BT);
        } else {
            startDiscovery();
        }

    }

    public void startDiscovery() {
        mDeviceFoundReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mDevicesListAdapter.addDevice(device);
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mDeviceFoundReceiver, intentFilter);

        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case R_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    startDiscovery();
                } else {
                    Toast.makeText(DevicesDiscoveryActivity.this, R.string.toast_bt_not_enable, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CANCELED);
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        Logger.i("onDestroy");
        super.onDestroy();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        if (mDeviceFoundReceiver != null) {
            unregisterReceiver(mDeviceFoundReceiver);
        }
    }

    /**
     * 每个item被点击后调用，在此方法中setResult
     * @param position
     */

    @Override
    public void onClick(int position) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE, mDevicesListAdapter.getDevice(position));
        setResult(RESULT_OK, intent);
        finish();
    }

}

class DevicesListAdapter extends RecyclerView.Adapter<DeviceViewHolder> {

    private List<BluetoothDevice> mDataList = new ArrayList<>();

    private OnItemClickListener mOnItemClickListener;

    private Context mContext;

    public DevicesListAdapter(Context context) {
        this.mContext = context;
    }

    public void setOnItemListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Logger.i("onCreateViewHolder");
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.device_item, parent, false);
        return new DeviceViewHolder(view, mOnItemClickListener);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        BluetoothDevice device = mDataList.get(position);
        String name = device.getName();
        String addr = device.getAddress();
        holder.setDeviceName(name == null ? mContext.getResources().getString(R.string.unknown_device) : name);
        holder.setDeviceAddress(addr);
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public void addDevice(BluetoothDevice device) {
        mDataList.add(device);
        notifyItemInserted(mDataList.size() - 1);
    }

    public BluetoothDevice getDevice(int position) {
        return mDataList.get(position);
    }

}

interface OnItemClickListener {
    void onClick(int position);
}

class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private View mMainView;
    private TextView tv_name;
    private TextView tv_addr;

    private OnItemClickListener mListener;

    public DeviceViewHolder(View itemView, OnItemClickListener l) {
        super(itemView);
        this.mListener = l;
        mMainView = itemView;
        mMainView.findViewById(R.id.device_item_cardview).setOnClickListener(this);
        tv_name = (TextView) mMainView.findViewById(R.id.device_item_name);
        tv_addr = (TextView) mMainView.findViewById(R.id.device_item_addr);
    }

    public DeviceViewHolder setDeviceName(String name) {
        this.tv_name.setText(name);
        return this;
    }

    public DeviceViewHolder setDeviceAddress(String addr) {
        this.tv_addr.setText(addr);
        return this;
    }

    @Override
    public void onClick(View v) {
        if (mListener != null) mListener.onClick(getLayoutPosition());
    }
}
