package huadi.ubikeplus.Drawer;

import huadi.ubikeplus.R;

import java.util.ArrayList;

public class DrawerListMoedel
{
	public static ArrayList<DrawerListItem> Items;

	public static void LoadModel()
	{
		Items = new ArrayList<DrawerListItem>();
		
		Items.add(new DrawerListItem(1, R.drawable.slide_menu_01, "個人概況"));		
		Items.add(new DrawerListItem(2, R.drawable.slide_menu_03, "附近站點"));
		Items.add(new DrawerListItem(3, R.drawable.slide_menu_02, "站點查詢"));
		Items.add(new DrawerListItem(4, R.drawable.slide_menu_04, "運動計時"));
		Items.add(new DrawerListItem(5, R.drawable.slide_menu_05, "功能設定"));
	}

	public static DrawerListItem GetbyId(int id)
	{
		for (DrawerListItem item : Items)
		{
			if (item.Id == id)
			{
				return item;
			}
		}
		return null;
	}
}
