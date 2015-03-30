
package org.zarroboogs.weibo.fragment;


import org.zarroboogs.weibo.widget.viewpagerfragment.ChildPage;
import org.zarroboogs.weibo.widget.viewpagerfragment.ViewPagerFragment;


import android.content.res.Resources;
import android.os.Bundle;
import android.util.SparseArray;

public class HotHuaTiViewPagerFragment extends ViewPagerFragment {

    public static HotHuaTiViewPagerFragment newInstance() {
        HotHuaTiViewPagerFragment fragment = new HotHuaTiViewPagerFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }
    

    @Override
	public SparseArray<ChildPage> buildChildPage() {
		// TODO Auto-generated method stub
		SparseArray<ChildPage> sparseArray = new SparseArray<ChildPage>();
		
		Resources re = getActivity().getResources();
		sparseArray.append(0, new ChildPage("1小时热榜", new HotHuaTiOneHourFragment()) );
		sparseArray.append(1, new ChildPage("电影热榜", new HotHuaTiFragmentFilm()) );
		sparseArray.append(2, new ChildPage("消费数码", new HotHuaTiFragmentDigit()) );
		sparseArray.append(3, new ChildPage("幽默搞笑", new HotHuaTiFragmentHumor()) );
		sparseArray.append(4, new ChildPage("IT互联网", new HotHuaTiFragmentIT()) );
		sparseArray.append(5, new ChildPage("摄影热榜",new HotHuaTiFragmentShot()) );
		sparseArray.append(6, new ChildPage("创意征集", new HotHuaTiFragmentOrig()) );
		sparseArray.append(7, new ChildPage("动物萌宠", new HotHuaTiFragmentPet()) );
		return sparseArray;
	}

	@Override
	public void onViewPageSelected(int id) {
		// TODO Auto-generated method stub
		
	}
}
