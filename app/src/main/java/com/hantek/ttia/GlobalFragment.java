package com.hantek.ttia;

import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by louie on 2014/1/3.
 */
public class GlobalFragment extends Fragment {
	private static final String TAG = GlobalFragment.class.getName();

	public void onBackPressed() {
		Log.v(TAG, "GlobalFragment getBack!!");
		if (getChildFragmentManager().getBackStackEntryCount() > 0) {
			getChildFragmentManager().popBackStack();
		}

		if (getFragmentManager().getBackStackEntryCount() > 0) {
			getFragmentManager().popBackStack();
		} else {
			// this.getActivity().finish();
		}
	}	
}
