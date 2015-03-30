
package org.zarroboogs.weibo.fragment;


import java.util.ArrayList;
import java.util.Arrays;

import org.zarroboogs.weibo.fragment.base.AbsBaseTimeLineFragment;
import org.zarroboogs.weibo.setting.SettingUtils;
import org.zarroboogs.weibo.support.utils.Utility;
import org.zarroboogs.weibo.widget.viewpagerfragment.ChildPage;
import org.zarroboogs.weibo.widget.viewpagerfragment.ViewPagerFragment;

import android.content.res.Resources;
import android.os.Bundle;

public class HotHuaTiViewPagerFragment extends ViewPagerFragment {

    public static HotHuaTiViewPagerFragment newInstance() {
        HotHuaTiViewPagerFragment fragment = new HotHuaTiViewPagerFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }
    

    @Override
	public ArrayList<ChildPage> buildChildPage() {
		// TODO Auto-generated method stub
    	ArrayList<ChildPage> sparseArray = new ArrayList<ChildPage>();
		
		Resources re = getActivity().getResources();
		sparseArray.add(0, new ChildPage("1小时热榜", new HotHuaTiOneHourFragment()) );
		sparseArray.add(1, new ChildPage("幽默搞笑", new HotHuaTiFragmentHumor()) );
		sparseArray.add(2, new ChildPage("电影热榜", new HotHuaTiFragmentFilm()) );
		sparseArray.add(3, new ChildPage("消费数码", new HotHuaTiFragmentDigit()) );

		sparseArray.add(4, new ChildPage("IT互联网", new HotHuaTiFragmentIT()) );
		sparseArray.add(5, new ChildPage("摄影热榜",new HotHuaTiFragmentShot()) );
		sparseArray.add(6, new ChildPage("创意征集", new HotHuaTiFragmentOrig()) );
		sparseArray.add(7, new ChildPage("动物萌宠", new HotHuaTiFragmentPet()) );
		
		String[] select = SettingUtils.getHotHuaTioSelected();
		Arrays.sort(select);
		
		ArrayList<ChildPage> result = new ArrayList<ChildPage>();
		for (int i = 0; i < select.length; i++) {
			
		}
		for (String string : select) {
			int key = Integer.valueOf(string);
			result.add(sparseArray.get(key));
		}
		
		return result;
	}

	@Override
	public void onViewPageSelected(int id) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void scrollToTop() {
		// TODO Auto-generated method stub
		BaseHotHuaTiFragment bf = (BaseHotHuaTiFragment) getCurrentFargment();
        Utility.stopListViewScrollingAndScrollToTop(bf.getListView());
	}
}
