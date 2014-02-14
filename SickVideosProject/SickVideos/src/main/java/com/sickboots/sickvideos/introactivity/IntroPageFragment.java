package com.sickboots.sickvideos.introactivity;

import android.app.Fragment;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sickboots.sickvideos.R;
import com.sickboots.sickvideos.imageutils.ToolbarIcons;

public class IntroPageFragment extends Fragment {

  public IntroPageFragment() {
    super();
  }

  public static IntroPageFragment newInstance(int sectionNumber) {
    IntroPageFragment fragment = new IntroPageFragment();
    Bundle args = new Bundle();
    args.putInt("sectionNumber", sectionNumber);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_intro_page, container, false);

    int sectionNumber = getArguments().getInt("sectionNumber");

    ActivityAccess access = (ActivityAccess) getActivity();
    IntroXMLParser.IntroPage page = access.pageAtIndex(sectionNumber);


    TextView titleView = (TextView) rootView.findViewById(R.id.title);
    TextView messageView = (TextView) rootView.findViewById(R.id.message);
    ImageView imageView = (ImageView) rootView.findViewById(R.id.image_view);


    if (page != null) {
      String message = "";

      String title = page.title;

      Drawable icon = null;
      int iconSize = 64;
      int color = getActivity().getResources().getColor(R.color.intro_drawable_color);

      if (page.icon.equals("info"))
        icon = ToolbarIcons.icon(getActivity(), ToolbarIcons.IconID.UPLOADS, color, iconSize);
      else if (page.icon.equals("youtube"))
        icon = ToolbarIcons.icon(getActivity(), ToolbarIcons.IconID.YOUTUBE, color, iconSize);
      else
        icon = ToolbarIcons.icon(getActivity(), ToolbarIcons.IconID.HEART, color, iconSize);

      titleView.setText(title);
      messageView.setText(message);
      imageView.setImageDrawable(icon);

      // insert the fields
      ViewGroup fieldContainer = (ViewGroup) rootView.findViewById(R.id.field_container);

      for (IntroXMLParser.IntroPageField field : page.fields) {
        TextView textView = new TextView(getActivity());

        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        textView.setAutoLinkMask(Linkify.ALL);
        textView.setTextSize(18);
        textView.setText(field.text);

        fieldContainer.addView(textView);
      }

      // gets the content top centered
      View spacer = (View) rootView.findViewById(R.id.spacer_view);
      Display display = getActivity().getWindowManager().getDefaultDisplay();
      Point size = new Point();
      display.getSize(size);
      int offset = (int) (((float) size.y) * .2f);

      spacer.getLayoutParams().height = offset;
      spacer.setLayoutParams(spacer.getLayoutParams());
    } else
      titleView.setText("wtf?");

    return rootView;
  }

  public interface ActivityAccess {
    IntroXMLParser.IntroPage pageAtIndex(int position);

  }

}


/*



  switch (sectionNumber) {
      case 0:
        icon = ToolbarIcons.icon(getActivity(), ToolbarIcons.IconID.HEART, color, iconSize);

        title = "Thanks!";
        message = "I'm glad you found my app.  Here's a quick start guide on how to use it.  \n\nSwipe screen to the next page...";
        break;
      case 1:
        icon = ToolbarIcons.icon(getActivity(), ToolbarIcons.IconID.YOUTUBE, color, iconSize);
        title = "Why use this app?";
        message = "YouTube is great, but I find the app to be overwhelming.  This app is like a video magazine.  One subject with the top channels.";
        break;
      case 2:
        icon = ToolbarIcons.icon(getActivity(), ToolbarIcons.IconID.UPLOADS, color, iconSize);
        title = "General Usage";
        message = "Select a channel from the action bar on the top left.  Use the left drawer to navigate within the current channel.  Swipe cards away when your finished watching or not interested.";
        break;
      case 3:
        icon = ToolbarIcons.icon(getActivity(), ToolbarIcons.IconID.SEARCH, color, iconSize);
        title = "I need your help!";
        message = "I'm just getting started and want to create the best app possible, so please send your feedback.  Did I include the BEST channels?  Are there other channels you would want to see in an app?";
        break;
      case 4:
        icon = ToolbarIcons.icon(getActivity(), ToolbarIcons.IconID.FULLSCREEN, color, iconSize);
        title = "Contact Info";
        message = "sgehrman@gmail.com \n\n Please rate my app!";
        break;
    }

 */