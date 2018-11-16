package com.maning.mnimagebrowser;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.maning.imagebrowserlibrary.ImageEngine;
import com.maning.imagebrowserlibrary.MNImageBrowser;
import com.maning.imagebrowserlibrary.listeners.OnClickListener;
import com.maning.imagebrowserlibrary.listeners.OnLongClickListener;
import com.maning.imagebrowserlibrary.listeners.OnPageChangeListener;
import com.maning.imagebrowserlibrary.model.ImageBrowserConfig;
import com.maning.mndialoglibrary.MStatusDialog;
import com.maning.mndialoglibrary.MToast;
import com.maning.mnimagebrowser.dialog.ListFragmentDialog;
import com.maning.mnimagebrowser.engine.GlideImageEngine;
import com.maning.mnimagebrowser.engine.PicassoImageEngine;
import com.maning.mnimagebrowser.utils.BitmapUtils;
import com.maning.mnimagebrowser.utils.DatasUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
	private static final String TAG="MainActivity";
	protected GridView gvImages;
	private ArrayList<String> sourceImageList;
	private Context context;
	public ImageBrowserConfig.TransformType transformType=
			ImageBrowserConfig.TransformType.Transform_Default;
	public ImageBrowserConfig.IndicatorType indicatorType=
			ImageBrowserConfig.IndicatorType.Indicator_Number;
	public ImageBrowserConfig.ScreenOrientationType screenOrientationType=
			ImageBrowserConfig.ScreenOrientationType.Screenorientation_Default;
	private ImageEngine imageEngine=new GlideImageEngine();
	//显示自定义遮盖层
	private boolean showCustomShadeView=false;
	//显示ProgressView
	private boolean showCustomProgressView=false;
	//是不是全屏模式
	private boolean isFulScreenMode=true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context=this;
		gvImages=(GridView)findViewById(R.id.gv_images);
		sourceImageList=DatasUtils.getDatas();
		gvImages.setAdapter(new NineGridAdapter());
	}
	
	private class NineGridAdapter extends BaseAdapter
	{
		@Override
		public int getCount(){
			return sourceImageList.size();
		}
		
		@Override
		public Object getItem(int position){
			return sourceImageList.get(position);
		}
		
		@Override
		public long getItemId(int position){
			return 0;
		}
		
		@Override
		public View getView(final int position, View convertView, ViewGroup viewGroup){
			final ViewHolder viewHolder;
			if(convertView==null){
				viewHolder=new ViewHolder();
				convertView=LayoutInflater.from(context).inflate(R.layout.image_item, null);
				viewHolder.imageView=(ImageView)convertView.findViewById(R.id.imageView);
				convertView.setTag(viewHolder);
			}else{
				viewHolder=(ViewHolder)convertView.getTag();
			}
			Picasso.with(context)
					.load(sourceImageList.get(position))
					.placeholder(R.drawable.default_placeholder)
					.error(R.mipmap.ic_launcher)
					.into(viewHolder.imageView, new Callback()
					{
						@Override
						public void onSuccess(){
							Log.i("----", "onSuccess:"+position);
						}
						
						@Override
						public void onError(){
							Log.i("----", "onError:"+position);
						}
					});
			viewHolder.imageView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view){
					//获取一个自定义遮盖层View
					View customView=
							LayoutInflater.from(context).inflate(R.layout.layout_custom_view, null);
					ImageView ic_close=(ImageView)customView.findViewById(R.id.iv_close);
					ImageView iv_more=(ImageView)customView.findViewById(R.id.iv_more);
					ImageView iv_comment=(ImageView)customView.findViewById(R.id.iv_comment);
					ImageView iv_zan=(ImageView)customView.findViewById(R.id.iv_zan);
					ImageView iv_delete=(ImageView)customView.findViewById(R.id.iv_delete);
					final TextView tv_number_indicator=
							(TextView)customView.findViewById(R.id.tv_number_indicator);
					ic_close.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v){
							//关闭图片浏览
							MNImageBrowser.finishImageBrowser();
						}
					});
					iv_zan.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v){
							MToast.makeTextShort(context, "点赞"+MNImageBrowser.getCurrentPosition());
						}
					});
					iv_more.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v){
							FragmentActivity currentActivity=MNImageBrowser.getCurrentActivity();
							ImageView currentImageView=MNImageBrowser.getCurrentImageView();
							if(currentImageView!=null && currentActivity!=null){
								showListDialog(currentActivity, currentImageView);
							}
						}
					});
					iv_comment.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v){
							MToast.makeTextShort(context, "评论"+MNImageBrowser.getCurrentPosition());
						}
					});
					iv_delete.setOnClickListener(new View.OnClickListener()
					{
						@Override
						public void onClick(View v){
							//删除当前位置的图片
							MNImageBrowser.removeCurrentImage();
							//刷新指示器
							tv_number_indicator.setText((MNImageBrowser.getCurrentPosition()+1)+"/"+
							                            MNImageBrowser.getImageList().size());
						}
					});
					tv_number_indicator.setText((position+1)+"/"+sourceImageList.size());
					MNImageBrowser.with(context)
							//页面切换效果
							.setTransformType(transformType)
							//指示器效果
							.setIndicatorType(indicatorType)
							//设置隐藏指示器
							.setIndicatorHide(false)
							//设置自定义遮盖层，定制自己想要的效果，当设置遮盖层后，原本的指示器会被隐藏
							.setCustomShadeView(showCustomShadeView? customView: null)
							//自定义ProgressView，不设置默认默认没有
							.setCustomProgressViewLayoutID(
									showCustomProgressView? R.layout.layout_custom_progress_view: 0)
							//当前位置
							.setCurrentPosition(position)
							//图片引擎
							.setImageEngine(imageEngine)
							//图片集合
							.setImageList(sourceImageList)
							//方向设置
							.setScreenOrientationType(screenOrientationType)
							//点击监听
							.setOnClickListener(new OnClickListener()
							{
								@Override
								public void onClick(FragmentActivity activity, ImageView view,
										int position, String url){
								}
							})
							//长按监听
							.setOnLongClickListener(new OnLongClickListener()
							{
								@Override
								public void onLongClick(final FragmentActivity activity,
										final ImageView imageView, int position, String url){
									showListDialog(activity, imageView);
								}
							})
							//页面切换监听
							.setOnPageChangeListener(new OnPageChangeListener()
							{
								@Override
								public void onPageSelected(int position){
									Log.i(TAG, "onPageSelected:"+position);
									if(tv_number_indicator!=null){
										tv_number_indicator.setText((position+1)+"/"+
										                            MNImageBrowser.getImageList()
												                            .size());
									}
								}
							})
							//全屏模式
							.setFullScreenMode(isFulScreenMode)
							//显示：传入当前View
							.show(viewHolder.imageView);
				}
			});
			return convertView;
		}
		
		public final class ViewHolder
		{
			ImageView imageView;
		}
	}
	
	private void showListDialog(final FragmentActivity activity, final ImageView imageView){
		new ListFragmentDialog(new ListFragmentDialog.OnItemClickListener()
		{
			@Override
			public void onClick(int position){
				if(position==1){
					//检查权限
					AndPermission.with(activity)
							.permission(Permission.WRITE_EXTERNAL_STORAGE)
							.onGranted(new Action()
							{
								@Override
								public void onAction(List<String> permissions){
									//保存图片
									imageView.buildDrawingCache(true);
									imageView.buildDrawingCache();
									Bitmap bitmap=imageView.getDrawingCache();
									String path=
											Environment.getExternalStorageDirectory()+"/test.png";
									boolean flag=BitmapUtils.saveBitmap(bitmap, path);
									imageView.setDrawingCacheEnabled(false);
									if(flag){
										new MStatusDialog(activity).show("保存成功",
												activity.getResources()
														.getDrawable(R.drawable.icon_save_success));
									}else{
										new MStatusDialog(activity).show("保存失败",
												activity.getResources()
														.getDrawable(R.drawable.icon_save_fail));
									}
								}
							})
							.onDenied(new Action()
							{
								@Override
								public void onAction(List<String> permissions){
									new MStatusDialog(activity).show("权限获取失败",
											activity.getResources()
													.getDrawable(R.drawable.icon_save_fail));
								}
							})
							.start();
				}
			}
		}).show(activity.getSupportFragmentManager(), "");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
		case R.id.menu_01:
			transformType=ImageBrowserConfig.TransformType.Transform_Default;
			break;
		case R.id.menu_02:
			transformType=ImageBrowserConfig.TransformType.Transform_DepthPage;
			break;
		case R.id.menu_03:
			transformType=ImageBrowserConfig.TransformType.Transform_RotateDown;
			break;
		case R.id.menu_04:
			transformType=ImageBrowserConfig.TransformType.Transform_RotateUp;
			break;
		case R.id.menu_05:
			transformType=ImageBrowserConfig.TransformType.Transform_ZoomIn;
			break;
		case R.id.menu_06:
			transformType=ImageBrowserConfig.TransformType.Transform_ZoomOutSlide;
			break;
		case R.id.menu_07:
			transformType=ImageBrowserConfig.TransformType.Transform_ZoomOut;
			break;
		case R.id.menu_08:
			indicatorType=ImageBrowserConfig.IndicatorType.Indicator_Number;
			break;
		case R.id.menu_09:
			indicatorType=ImageBrowserConfig.IndicatorType.Indicator_Circle;
			break;
		case R.id.menu_10:
			imageEngine=new GlideImageEngine();
			break;
		case R.id.menu_11:
			imageEngine=new PicassoImageEngine();
			break;
		case R.id.menu_12:
			screenOrientationType=
					ImageBrowserConfig.ScreenOrientationType.Screenorientation_Default;
			break;
		case R.id.menu_13:
			screenOrientationType=
					ImageBrowserConfig.ScreenOrientationType.ScreenOrientation_Portrait;
			break;
		case R.id.menu_14:
			screenOrientationType=
					ImageBrowserConfig.ScreenOrientationType.Screenorientation_Landscape;
			break;
		case R.id.menu_15:
			showCustomShadeView=true;
			break;
		case R.id.menu_16:
			showCustomShadeView=false;
			break;
		case R.id.menu_17:
			showCustomProgressView=false;
			break;
		case R.id.menu_18:
			showCustomProgressView=true;
			break;
		case R.id.menu_19:
			isFulScreenMode=true;
			break;
		case R.id.menu_20:
			isFulScreenMode=false;
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
