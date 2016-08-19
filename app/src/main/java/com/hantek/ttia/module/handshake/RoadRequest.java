package com.hantek.ttia.module.handshake;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import com.hantek.ttia.module.roadutils.Road;
import com.hantek.ttia.module.roadutils.RoadDataFactory;
import com.hantek.ttia.module.roadutils.RoadManager;
import com.hantek.ttia.module.roadutils.RoadUpdater;

import component.LogManager;

public class RoadRequest implements IRequest {
    static final String TAG = RoadRequest.class.getName();

    private String customerID = "";
    private Handler mHandler;
    // private List<Road> cloudRoadData;
    private Context mContext;

    private void initRoadRequest() {
        RoadManager.getInstance().setRoadData(RoadDataFactory.loadRoadData());
    }

    public RoadRequest(Context context) {
        initRoadRequest();
        mContext = context;
    }

    public RoadRequest(Context context, Handler handler) {
        initRoadRequest();
        mContext = context;
        mHandler = handler;
    }

    public void setCustomerID(String cID) {
        customerID = cID;
    }

    @Override
    public boolean checkConnection() {
        return true;
    }

    @Override
    public boolean request() {
        List<Road> cloudRoadData = RoadUpdater.getCloudRoadData(customerID);

        if (cloudRoadData == null)
            return false;
        else {
            try {
                List<Road> localRoadData = RoadManager.getInstance().getLocalRoadData();
                Log.d(TAG, String.format("Cloud total:%s, Local total:%s", cloudRoadData.size(), localRoadData.size()));

                List<Road> downloadList = new ArrayList<Road>();
                for (Road remote : cloudRoadData) {
                    boolean update = true;
                    for (Road local : localRoadData) {
                        if (local.id == remote.id && local.branch.equalsIgnoreCase(remote.branch) && local.direct == remote.direct) {
                            if (local.version == remote.version) {
                                // 版本相同 不需更新
                                update = false;
                                Log.d(TAG, "Same version " + local.toString());
                                break;
                            }
                        }
                    }

                    if (update) {
                        downloadList.add(remote);
                    }
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<Road> removeList = new ArrayList<Road>();
                // 移除路線
                for (Road local : localRoadData) {
                    boolean foundData = false;
                    for (Road remote : cloudRoadData) {
                        if (local.id == remote.id && local.branch.equalsIgnoreCase(remote.branch) && local.direct == remote.direct) {
                            foundData = true;
                            break;
                        }
                    }

                    if (!foundData)
                        removeList.add(local);
                }

                if (removeList.size() > 0) {
                    for (Road road : removeList) {
                        String fileName = String.format("%04d%s%s.txt", road.id, road.branch, road.direct);
                        RoadDataFactory.deleteFile(fileName);
                    }
                }

                for (Road road : downloadList) {
                    String fileName = String.format("%04d%s%s.txt", road.id, road.branch, road.direct);
                    RoadUpdater.fetchRoadFile(fileName, customerID);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogManager.write("road", String.format("*** fetch Road data, %s.%s ***", e.toString(), customerID), null);
            }
            return true;
        }
    }

    @Override
    public boolean waitResp() {
        return true;
    }

    @Override
    public Object getResponse() {
        return "檢查中...";
    }

    @Override
    public void execute(Object data) {
        RoadManager.getInstance().setRoadData(RoadDataFactory.loadRoadData());
//        mHandler.sendMessage(mHandler.obtainMessage(SystemCheckActivity.HDL_CONNECTED));
        displayResult();
    }

    @Override
    public void rollBack() {
//        mHandler.sendMessage(mHandler.obtainMessage(SystemCheckActivity.HDL_DISCONNECTED));
        displayResult();
    }

    private void displayResult() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//        StringBuilder msg = new StringBuilder();
//        builder.setTitle("路線檔");
//        msg.append(String.format("總數:%s\n", RoadUpdater.cloudData));
//        builder.setMessage(msg.toString());
//        builder.setNegativeButton("Confirm", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int which) {
//
//            }
//        });
//        builder.setCancelable(true);
//        builder.create().show();
    }
}