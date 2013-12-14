package com.sickboots.sickvideos;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewCallback;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.sickboots.sickvideos.database.DatabaseAccess;
import com.sickboots.sickvideos.database.YouTubeData;
import com.sickboots.sickvideos.misc.ApplicationHub;
import com.sickboots.sickvideos.misc.PreferenceCache;
import com.sickboots.sickvideos.misc.StandardAnimations;
import com.sickboots.sickvideos.misc.Util;
import com.sickboots.sickvideos.services.YouTubeServiceRequest;
import com.sickboots.sickvideos.youtube.VideoImageView;
import com.sickboots.sickvideos.youtube.YouTubeAPI;

import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.ISOPeriodFormat;
import org.joda.time.format.PeriodFormatter;

public class YouTubeCursorAdapter extends SimpleCursorAdapter implements AdapterView.OnItemClickListener, VideoMenuView.VideoMenuViewListener {
  private final LayoutInflater inflater;
  private int animationID = 0;
  private boolean showHidden = false;
  private Context mContext;
  private final YouTubeData mReusedData = new YouTubeData(); // avoids a memory alloc when drawing
  private Theme mTheme;
  private YouTubeServiceRequest mRequest;
  private YouTubeCursorAdapterListener mListener;

  public interface YouTubeCursorAdapterListener {
    public void handleClickFromAdapter(YouTubeData itemMap);
  }

  public static YouTubeCursorAdapter newAdapter(Context context, YouTubeServiceRequest request, YouTubeCursorAdapterListener listener) {
    Theme theme = newTheme(context);

    String[] from = new String[]{};
    int[] to = new int[]{};

    YouTubeCursorAdapter result = new YouTubeCursorAdapter(context, theme.mTheme_resId, null, from, to, 0);
    result.mTheme = theme;
    result.mRequest = request;
    result.mContext = context.getApplicationContext();
    result.mListener = listener;

    return result;
  }

  public ViewGroup rootView(ViewGroup container) {
    return (ViewGroup) inflater.inflate(mTheme.mTheme_resId, container, false);
  }

  private YouTubeCursorAdapter(Context context, int layout, Cursor c, String[] from,
                               int[] to, int flags) {
    super(context, layout, c, from, to, flags);

    inflater = LayoutInflater.from(context);
  }

  private void animateViewForClick(final View theView) {
    switch (animationID) {
      case 0:
        StandardAnimations.dosomething(theView);
        break;
      case 1:
        StandardAnimations.upAndAway(theView);
        break;
      case 2:
        StandardAnimations.rockBounce(theView);
        break;
      case 3:
        StandardAnimations.winky(theView, mTheme.mTheme_imageAlpha);
        break;
      default:
        StandardAnimations.rubberClick(theView);
        animationID = -1;
        break;
    }

    animationID += 1;
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    ViewHolder holder = (ViewHolder) v.getTag();

    if (holder != null) {
      animateViewForClick(holder.image);

      Cursor cursor = (Cursor) getItem(position);
      YouTubeData itemMap = mRequest.databaseTable().cursorToItem(cursor, null);

      mListener.handleClickFromAdapter(itemMap);
    } else {
      Util.log("no holder on click?");
    }
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder = null;

    if (convertView == null) {
      convertView = inflater.inflate(mTheme.mTheme_itemResId, null);

      holder = new ViewHolder();
      holder.image = (VideoImageView) convertView.findViewById(R.id.image);
      holder.title = (TextView) convertView.findViewById(R.id.text_view);
      holder.description = (TextView) convertView.findViewById(R.id.description_view);
      holder.duration = (TextView) convertView.findViewById(R.id.duration);
      holder.menuButton = (VideoMenuView) convertView.findViewById(R.id.menu_button);

      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();

      // reset some stuff that might have been set on an animation
      holder.image.setAlpha(1.0f);
      holder.image.setScaleX(1.0f);
      holder.image.setScaleY(1.0f);
      holder.image.setRotationX(0.0f);
      holder.image.setRotationY(0.0f);
    }

    Cursor cursor = (Cursor) getItem(position);
    YouTubeData itemMap = mRequest.databaseTable().cursorToItem(cursor, mReusedData);

    holder.image.setAnimation(null);

    holder.image.setDrawShadows(mTheme.mTheme_drawImageShadows);

    int defaultImageResID = 0;

    UrlImageViewHelper.setUrlDrawable(holder.image, itemMap.mThumbnail, defaultImageResID, new UrlImageViewCallback() {

      @Override
      public void onLoaded(ImageView imageView, Bitmap loadedBitmap, String url, boolean loadedFromCache) {
        if (!loadedFromCache) {
          imageView.setAlpha(0.0f); // mTheme_imageAlpha / 2);
          imageView.animate().setDuration(200).alpha(mTheme.mTheme_imageAlpha);
        } else
          imageView.setAlpha(mTheme.mTheme_imageAlpha);
      }

    });

    boolean hidden = false;
    if (!showHidden)
      hidden = itemMap.isHidden();

    if (hidden)
      holder.title.setText("(Hidden)");
    else
      holder.title.setText(itemMap.mTitle);

    String duration = itemMap.mDuration;
    if (duration != null) {
      holder.duration.setVisibility(View.VISIBLE);

      PeriodFormatter formatter = ISOPeriodFormat.standard();
      Period p = formatter.parsePeriod(duration);
      Seconds s = p.toStandardSeconds();

      holder.duration.setText(Util.millisecondsToDuration(s.getSeconds() * 1000));
    } else {
      holder.duration.setVisibility(View.GONE);
    }

    // hide description if empty
    if (holder.description != null) {
      String desc = (String) itemMap.mDescription;
      if (desc != null && (desc.length() > 0)) {
        holder.description.setVisibility(View.VISIBLE);
        holder.description.setText(desc);
      } else {
        holder.description.setVisibility(View.GONE);
      }
    }

    // set video id on menu button so clicking can know what video to act on
    // only set if there is a videoId, playlists and others don't need this menu
    String videoId = (String) itemMap.mVideo;
    if (videoId != null && (videoId.length() > 0)) {
      holder.menuButton.setVisibility(View.VISIBLE);
      holder.menuButton.setListener(this);
      holder.menuButton.mId = itemMap.mID;
    } else {
      holder.menuButton.setVisibility(View.GONE);
    }

    return convertView;
  }

  // VideoMenuViewListener
  @Override
  public void showVideoInfo(Long itemId) {
    DatabaseAccess database = new DatabaseAccess(mContext, mRequest);
    YouTubeData videoMap = database.getItemWithID(itemId);

    if (videoMap != null) {
      Util.log("fix me");
    }
  }

  // VideoMenuViewListener
  @Override
  public void showVideoOnYouTube(Long itemId) {
    DatabaseAccess database = new DatabaseAccess(mContext, mRequest);
    YouTubeData videoMap = database.getItemWithID(itemId);

    if (videoMap != null)
      YouTubeAPI.playMovieUsingIntent(mContext, videoMap.mVideo);
  }

  // VideoMenuViewListener
  @Override
  public void hideVideo(Long itemId) {
    DatabaseAccess database = new DatabaseAccess(mContext, mRequest);
    YouTubeData videoMap = database.getItemWithID(itemId);

    if (videoMap != null) {
      videoMap.setHidden(!videoMap.isHidden());
      database.updateItem(videoMap);

    }
  }

  private static Theme newTheme(Context context) {
    Theme result = new Theme();

    String themeStyle = ApplicationHub.preferences(context).getString(PreferenceCache.THEME_STYLE, "0");
    if (Integer.parseInt(themeStyle) != 0) {
      result.mTheme_itemResId = R.layout.youtube_item_cards;
      result.mTheme_imageAlpha = 1.0f;
      result.mTheme_drawImageShadows = false;
      result.mTheme_resId = R.layout.fragment_grid_cards;
    } else {
      result.mTheme_imageAlpha = 1.0f;
      result.mTheme_itemResId = R.layout.youtube_item_dark;
      result.mTheme_drawImageShadows = true;
      result.mTheme_resId = R.layout.fragment_grid_dark;
    }

    return result;
  }

  static class Theme {
    float mTheme_imageAlpha;
    int mTheme_itemResId;
    int mTheme_resId;
    boolean mTheme_drawImageShadows;
  }

  static class ViewHolder {
    TextView title;
    TextView description;
    TextView duration;
    VideoImageView image;
    VideoMenuView menuButton;
  }
}
