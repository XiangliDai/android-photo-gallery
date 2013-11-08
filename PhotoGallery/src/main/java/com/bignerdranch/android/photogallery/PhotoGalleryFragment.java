package com.bignerdranch.android.photogallery;


import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by xdai on 11/7/13.
 */
public class PhotoGalleryFragment extends Fragment implements AbsListView.OnScrollListener {

    GridView mGridView;
    ArrayList<GalleryItem> mItems;
    private Integer mPage = 1;
    private static final String TAG = "PhotoGalleryFragment";
    ThumbnailDownloader<ImageView> mThumbnailThread;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        new FetchItemsTask().execute(mPage);

        mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
                if(isVisible())
                    imageView.setImageBitmap(thumbnail);
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        Log.i(TAG, "Background thread started");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container,false);

        mGridView = (GridView)v.findViewById(R.id.gridView);
        //mGridView.setOnScrollListener(this);
        setupAdapter();
        return v;
    }

    public void onDestroyView(){
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }

    public void onDestroy(){
        super.onDestroy();
        mThumbnailThread.quit();
        Log.i(TAG, "Background thread destroyed");
    }
    void setupAdapter(){
        if(getActivity() == null || mGridView == null) return;
        if(mItems != null){
            mGridView.setAdapter(new GalleryItemAdapter(mItems));
            //mGridView.setAdapter(new ArrayAdapter<GalleryItem>(getActivity(), android.R.layout.simple_gallery_item, mItems));
        }
        else
            mGridView.setAdapter(null);
    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        mPage++;
        //new FetchItemsTask().execute(mPage);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }


    private class FetchItemsTask extends AsyncTask<Integer, Void, ArrayList<GalleryItem>>{
        protected ArrayList<GalleryItem> doInBackground(Integer... params){
            return new FlickrFetchr().fetchItems(params[0]);
        }

        protected void onPostExecute(ArrayList<GalleryItem> items){
            mItems = items;
            setupAdapter();
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>{

        public GalleryItemAdapter(ArrayList<GalleryItem> items) {
            super(getActivity(), 0, items);

        }

        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item, parent,false);

            }
            ImageView imageView = (ImageView)convertView.findViewById(R.id.galley_item_imageView);
            imageView.setImageResource(R.drawable.abc_ic_search);
            GalleryItem item = getItem(position);
            Log.i(TAG, "mThumbnailThread queueThumbnail");
            mThumbnailThread.queueThumbnail(imageView, item.getUrl());
            return convertView;
        }
    }

}
