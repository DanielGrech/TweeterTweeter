package com.DGSD.TweeterTweeter.UI;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class LinkEnabledTextView  extends TextView{
    public static final int MATCH_TWITTER = 0;
    public static final int MATCH_ALL = 1;
    
    // The String Containing the Text that we have to gather links from private SpannableString linkableText;
    // Populating and gathering all the links that are present in the Text
    private Vector<Hyperlink> listOfLinks; 

    // A Listener Class for generally sending the Clicks to the one which requires it
    TextLinkClickListener mListener;

    // Pattern for gathering @usernames from the Text
    Pattern screenNamePattern = Pattern.compile("(@[a-zA-Z0-9_]+)");

    // Pattern for gathering #hasttags from the Text
    Pattern hashTagsPattern = Pattern.compile("(#[a-zA-Z0-9_-]+)");

    // Pattern for gathering http:// links from the Text
    Pattern hyperLinksPattern = Pattern.compile("\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");
    
    //Match all text..
    Pattern matchAll = Pattern.compile("[a-zA-Z0-9_-]+");
    
    public LinkEnabledTextView(Context context, AttributeSet attrs){
	super(context, attrs);
	listOfLinks = new Vector<Hyperlink>();

    }

    public void gatherLinksForText(String text, int match){
	/*
	 * Ensure that if this view is reused, we dont have any old data in here!
	 */
	listOfLinks = null;
	listOfLinks = new Vector<Hyperlink>();
	
	SpannableString linkableText = new SpannableString(text);
	/*
	 *  gatherLinks basically collects the Links depending upon the Pattern that we supply
	 *  and add the links to the ArrayList of the links
	 */
	gatherLinks(linkableText, screenNamePattern);
	gatherLinks(linkableText, hashTagsPattern);
	gatherLinks(linkableText, hyperLinksPattern);
	
	if(match == MATCH_ALL)
	    gatherLinks(linkableText, matchAll);

	for(int i = 0, size = listOfLinks.size(); i< size; i++){
	    Hyperlink linkSpec = listOfLinks.elementAt(i);
	    /*
	     * this process here makes the Clickable Links from the text
	     */
	    linkableText.setSpan(linkSpec.span, linkSpec.start, linkSpec.end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	/*
	 * sets the text for the TextView with enabled links
	 */
	setText(linkableText);
    }

    /*
     * sets the Listener for later click propagation purpose
     */
    public void setOnTextLinkClickListener(TextLinkClickListener newListener){
	mListener = newListener;
    }

    /*
     * The Method mainly performs the Regex Comparison for the Pattern and adds them to
     * listOfLinks array list
     */

    private final void gatherLinks(Spannable s, Pattern pattern){
	// Matcher matching the pattern
	Matcher m = pattern.matcher(s);

	while (m.find()){
	    int start = m.start();
	    int end = m.end();

	    /*
	     *  Hyperlink is basically used like a structure for storing the information about
	     *  where the link was found.
	     */
	    Hyperlink spec = new Hyperlink();

	    String pat = s.subSequence(start, end).toString();
	    
	    if(pat.startsWith("(") && pat.endsWith(")")) {
	    	start+=1;
	    	end-=1;
	    	pat = pat.substring(1, pat.length() - 2);
	    	
		}
	    
	    
	    spec.textSpan = pat;
	    spec.span = new InternalURLSpan(pat);
	    spec.start = start;
	    spec.end = end;

	    listOfLinks.add(spec);
	}
    }

    /*
     * This is class which gives us the clicks on the links which we then can use.
     */
    public class InternalURLSpan extends ClickableSpan{
	private String clickedSpan;

	public InternalURLSpan (String clickedString){
	    clickedSpan = clickedString;
	}

	@Override
	public void onClick(View textView){
	    mListener.onTextLinkClick(textView, clickedSpan);
	}
    }

    /*
     * Class for storing the information about the Link Location
     */

    class Hyperlink{
	CharSequence textSpan;
	InternalURLSpan span;
	int start;
	int end;

    }

    public interface TextLinkClickListener{
	/*
	 *  This method is called when the TextLink is clicked from LinkEnabledTextView
	 */
	public void onTextLinkClick(View textView, String clickedString);
    }
}