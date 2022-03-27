package com.github.milochen0418.syncslide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.RequiresApi;

import java.net.URL;
import java.util.Random;


public class MainActivity extends Activity implements View.OnClickListener{


    private Context mSocketIoContextPop;
    private WebView mSocketIoWebViewPop;
    private AlertDialog mSocketIoBuilder;
    private String mSocketIoUserAgent;



    WebView mSocketIoView;
    WebView mSlideView;
    WebView mWebView;
    EditText mRoomNameText;
    private static final String TAG = "SSLIDE";
    @Override
    @SuppressLint("SetJavaScriptEnabled")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.page_1_btn).setOnClickListener(this);
        findViewById(R.id.page_2_btn).setOnClickListener(this);
        findViewById(R.id.page_3_btn).setOnClickListener(this);
        findViewById(R.id.create_room_btn).setOnClickListener(this);
        findViewById(R.id.join_room_btn).setOnClickListener(this);
        mRoomNameText = (EditText)findViewById(R.id.room_name_text);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {

        setupWebViews();
        super.onStart();
    }
    static public String sUrl = "";
    public boolean mIsSlideViewTouchDisable = false; 
    @Override
    public void onBackPressed() {

        if(mSocketIoView.canGoBack()) {
            mSocketIoView.goBack();
        }
        else {
            finishAffinity();
            System.exit(0);
        }
    }

    protected void setupWebViews() {
        Log.i(TAG, "setupWebViews call");
        mSlideLoadedUrl = "";
        try {
            mSocketIoView = findViewById(R.id.socketio_webview);
            mSlideView = findViewById(R.id.slide_webview);
            mSlideView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return MainActivity.this.mIsSlideViewTouchDisable;
                }
            });

        } catch(Exception e) {
            return;
        }




        WebSettings webSlideSettings = mSlideView.getSettings();
        webSlideSettings.setSaveFormData(false);
        webSlideSettings.setJavaScriptEnabled(true);
        webSlideSettings.setAllowFileAccessFromFileURLs(true);
        webSlideSettings.setAllowUniversalAccessFromFileURLs(true);
        webSlideSettings.setDomStorageEnabled(true); //允許使用localstorage

        
        CookieManager.getInstance().setAcceptCookie(true);
        if(android.os.Build.VERSION.SDK_INT >= 21)
            CookieManager.getInstance().setAcceptThirdPartyCookies(mSocketIoView, true);

        WebSettings webSocketioSettings = mSocketIoView.getSettings();
        webSocketioSettings.setSaveFormData(false);
        webSocketioSettings.setJavaScriptEnabled(true);
        webSocketioSettings.setAllowFileAccessFromFileURLs(true);
        webSocketioSettings.setAllowUniversalAccessFromFileURLs(true);
        webSocketioSettings.setDomStorageEnabled(true); //允許使用localstorage
        
        webSocketioSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSocketioSettings.setSupportMultipleWindows(true);

        mSocketIoContextPop = this.getApplicationContext();


        this.loadWebrtcUrl(mSlideView, "https://slide.covidicq.net/slide-wallet", new JSSlideInterface(MainActivity.this));
        String SlideCovidIcqUrl =  "https://slide.covidicq.net" + "#" + getRandomString(16);
        this.loadWebrtcUrl(mSocketIoView, SlideCovidIcqUrl, new JSSocketIoInterface(this));
    }

    private static final String ALLOWED_CHARACTERS ="qwertyuiopasdfghjklzxcvbnm";
    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(sizeOfRandomString);
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }

    public void changeGuiByWebivewLoadStatus() {
        if (mIsWebviewLoadStatusError == true) {
        } else {
        }
    }

    public boolean mIsSyncSlideHost = false;

    public void callCreateRoom(final String externalUrl) {
        mIsSyncSlideHost = true;
        mIsSlideViewTouchDisable = false;

        mSocketIoView.loadUrl("javascript:triggerCreateRoom(\""+ externalUrl+ "\");");
    }

    public void callJoinRoom(final String roomName) {
        mIsSyncSlideHost = false;
        mIsSlideViewTouchDisable = true;
        mSocketIoView.loadUrl("javascript:triggerJoinRoom(\""+ roomName+ "\");");
    }

    public void controlSpeakerMuted( boolean muted ) {
        if(muted == true) {
            mSlideView.loadUrl("javascript:setLastRemoteVideoMuted(true);");
        } else {
            mSlideView.loadUrl("javascript:setLastRemoteVideoMuted(false);");
        }
    }

    WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            view.loadUrl("javascript:window.android.onUrlChange(window.location.href);");
        };
    };

    class MyJavaScriptInterface {
        @JavascriptInterface
        public void onUrlChange(String url) {
            
        }
    }

    public boolean mIsWebviewLoadStatusError = false;
    public String mLoadedUrl = "";
    public String mSlideLoadedUrl = "";
    public boolean mIsInPipMode = false;
    public boolean mIsOpenSound = false;
    public String mSlideViewUrl = "";
    public void processSlideViewUrlChange(final String newURL) {
        Log.i(TAG, "slide newURL = " + newURL);

        if(mIsSyncSlideHost == true) {
            mSocketIoView.loadUrl("javascript:triggerMulticastUrl(\""+ newURL+ "\");");
        } else {
        
        }
    }

    class CustomSocketIoChromeClient extends ChromeClient {

        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog,
                                      boolean isUserGesture, Message resultMsg) {

            mSocketIoWebViewPop = new WebView(mSocketIoContextPop);
            mSocketIoWebViewPop.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }
            });

            // Enable Cookies
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            if(android.os.Build.VERSION.SDK_INT >= 21) {
                cookieManager.setAcceptThirdPartyCookies(mSocketIoWebViewPop, true);
                cookieManager.setAcceptThirdPartyCookies(mSocketIoView, true);
            }

            WebSettings popSettings = mSocketIoWebViewPop.getSettings();
            
            mSocketIoWebViewPop.setVerticalScrollBarEnabled(false);
            mSocketIoWebViewPop.setHorizontalScrollBarEnabled(false);
            popSettings.setJavaScriptEnabled(true);
            popSettings.setSaveFormData(true);
            popSettings.setEnableSmoothTransition(true);
            // Set User Agent
            popSettings.setUserAgentString(mSocketIoUserAgent + "Your App Info/Version");
            
            popSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

            
            mSocketIoWebViewPop.setWebChromeClient(new CustomSocketIoChromeClient());

            
            mSocketIoBuilder = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT).create();
            mSocketIoBuilder.setTitle("");
            mSocketIoBuilder.setView(mSocketIoWebViewPop);

            mSocketIoBuilder.setButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    mSocketIoWebViewPop.destroy();
                    dialog.dismiss();
                }
            });

            mSocketIoBuilder.show();
            mSocketIoBuilder.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mSocketIoWebViewPop);
            resultMsg.sendToTarget();

            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            try {
                mSocketIoWebViewPop.destroy();
            } catch (Exception e) {
            }

            try {
                mSocketIoBuilder.dismiss();
            } catch (Exception e) {
            }

        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            if (!title.equals(view.getUrl())) {
                mIsWebviewLoadStatusError = true;
            } else {
                mIsWebviewLoadStatusError = false ;
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                public void run() {
                    changeGuiByWebivewLoadStatus();
                }
            } , 10);
            super.onReceivedTitle(view, title);
        }


        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return true;
        }
    }

    @SuppressLint("JavascriptInterface")
    public void loadWebrtcUrl(final WebView theWebView, final String url, final Object jsInterface){
        theWebView.setWebContentsDebuggingEnabled(true); //set for debug
        if(theWebView == MainActivity.this.mSlideView) {
            theWebView.setWebChromeClient( new ChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    if (view.getUrl().equals(mSlideViewUrl)) {

                    } else {
                        mSlideViewUrl = view.getUrl();
                        processSlideViewUrlChange(mSlideViewUrl);
                    }
                    super.onProgressChanged(view, newProgress);
                }

                @Override
                public void onReceivedTitle(WebView view, String title) {
                    if (!title.equals(view.getUrl())) {
                        mIsWebviewLoadStatusError = true;
                    } else {
                        mIsWebviewLoadStatusError = false ;
                    }
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable(){
                        public void run() {
                            changeGuiByWebivewLoadStatus();
                        }
                    } , 10);
                    super.onReceivedTitle(view, title);
                }

                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    return true;
                }


                @Override
                public void onPermissionRequest(final PermissionRequest request) {
                    try {
                        final URL urlobj = new URL(url);
                        final String hostName = urlobj.getHost();
                        final String protocolName = urlobj.getProtocol();
                        final String webdomainName = String.format("%s://%s/", protocolName, hostName);
                        Log.d(TAG, webdomainName);
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void run() {
                                Log.d(TAG, request.getOrigin().toString());
                                if (request.getOrigin().toString().equals(webdomainName)) {
                                    String[] strs = request.getResources();
                                    Log.d(TAG, String.join(",", strs));
                                    request.grant(request.getResources());
                                } else {
                                    request.deny();
                                }
                            }
                        });
                    }
                    catch(Exception e) {
                    }
                }
            });


        } else {
            theWebView.setWebChromeClient( new CustomSocketIoChromeClient());
        }

        theWebView.loadUrl(url);


        final String pureUrl = (url.split("#")[0]).split("\\?")[0];
        if( mSlideLoadedUrl.equals(pureUrl) || mSlideLoadedUrl.equals("")) {
            final Handler handler1 = new Handler(Looper.getMainLooper());
            handler1.postDelayed(r, 100);
        } 
        mSlideLoadedUrl = pureUrl;

        try {
            theWebView.addJavascriptInterface(jsInterface, "android_webview");            
        } catch(Exception e) {
        }
    }
    
    String mExternalUrl = "";
    @Override
    public void onClick(View view) {
        int vid = view.getId();
        switch(vid) {
            case R.id.page_1_btn:
                
                break;
            case R.id.page_2_btn:
                
                break;
            case R.id.page_3_btn:
                
                break;
            case R.id.create_room_btn:
                final String externalUrl = mExternalUrl;
                this.callCreateRoom(externalUrl);
                break;
            case R.id.join_room_btn:
                String roomName = mRoomNameText.getText()+"";
                this.callJoinRoom(roomName);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm.isActive()) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_NOT_ALWAYS);
                }
                break;

        }

    }

    private class ChromeClient extends WebChromeClient {
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;
        ChromeClient() {}
        public Bitmap getDefaultVideoPoster()
        {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }
        public void onHideCustomView()
        {
            ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            try {
                this.mCustomViewCallback.onCustomViewHidden();
            } catch(Exception e) {

            }
            this.mCustomViewCallback = null;
        }
        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
        {
            if (this.mCustomView != null)
            {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private final class JSSocketIoInterface{
        Context mContext;
        JSSocketIoInterface(Context c) {
            mContext = c;
        }
        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void notifyRemoteURLWithPostMessage(final String message) {
            Log.i(TAG,"notifyRemoteURLWithPostMessage is invoked with message = " + message);
            new android.os.Handler(Looper.getMainLooper()).postDelayed( new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                public void run() {
                    String urlStr = message;
                    MainActivity.this.loadWebrtcUrl(mSlideView, urlStr, new JSSlideInterface(MainActivity.this));
                }
            }, 10);
        }


        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void createRoomFinished() {
            new android.os.Handler(Looper.getMainLooper()).postDelayed( new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                public void run() {
                    String externalUrl = MainActivity.this.mExternalUrl;
                    MainActivity.this.loadWebrtcUrl(mSlideView, externalUrl, new JSSlideInterface(MainActivity.this));
                }
            }, 10);
        }

        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void notifyRoomName(final String roomName) {
            new android.os.Handler(Looper.getMainLooper()).postDelayed( new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                public void run() {
                    String name = roomName;
                    MainActivity.this.mRoomNameText.setText(roomName);
                }
            }, 10);
        }


        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void requestReload() {
            new android.os.Handler(Looper.getMainLooper()).postDelayed( new Runnable() {
                public void run() {
                    String urlStr = MainActivity.sUrl;
                    MainActivity.this.loadWebrtcUrl(mSlideView, urlStr, new JSSlideInterface(MainActivity.this));
                }
            }, 1000);
        }

        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void postMessage(final String message){
            Log.i(TAG,"postMessage is invoked");
            if(!MainActivity.this.mIsInPipMode) {
                new AlertDialog.Builder(mContext)
                        .setTitle("溫馨小提醒")
                        .setMessage(message)
                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                            }
                        })
                        .show();
            }
        }
    }


    private final class JSSlideInterface{
        Context mContext;
        JSSlideInterface(Context c) {
            mContext = c;
        }
        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void notifyRemoteVideoOnPlayingWithPostMessage(final String message) {
            Log.i(TAG,"notifyRemoteVideoOnPlayingWithPostMessage is invoked");
            new android.os.Handler(Looper.getMainLooper()).postDelayed( new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                public void run() {
                    Log.i(TAG,"Processing notifyRemoteVideoOnPlayingWithPostMessage");
                    mIsInPipMode = MainActivity.this.isInPictureInPictureMode();
                    if(mIsInPipMode == true) {
                        if(mIsOpenSound == true) {
                            MainActivity.this.controlSpeakerMuted(false);
                        } else {
                            MainActivity.this.controlSpeakerMuted(true);
                        }
                    } else {
                        new AlertDialog.Builder(mContext)
                                .setTitle("溫馨小提醒")
                                .setMessage(message)
                                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Continue with delete operation
                                    }
                                })
                                .show();
                    }
                }
            }, 1000);
        }

        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void requestReload() {
            Log.i(TAG,"requestReload is invoked");
            new android.os.Handler(Looper.getMainLooper()).postDelayed( new Runnable() {
                public void run() {
                    //mWebView.reload();
                    //String urlStr = "https://accompany.covidicq.net/webview.html#oo";
                    String urlStr = MainActivity.sUrl;
                    Log.i(TAG,"urlStr = " + urlStr );
                    MainActivity.this.loadWebrtcUrl(mSlideView, urlStr, new JSSlideInterface(MainActivity.this));
                }
            }, 1000);
        }

        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void postMessage(final String message){
            Log.i(TAG,"postMessage is invoked");
            if(!MainActivity.this.mIsInPipMode) {
                new AlertDialog.Builder(mContext)
                        .setTitle("溫馨小提醒")
                        .setMessage(message)
                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                            }
                        })
                        .show();
            }
        }
    }



    private final class JSInterface{
        Context mContext;
        JSInterface(Context c) {
            mContext = c;
        }
        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void notifyRemoteVideoOnPlayingWithPostMessage(final String message) {
            Log.i(TAG,"notifyRemoteVideoOnPlayingWithPostMessage is invoked");
            new android.os.Handler(Looper.getMainLooper()).postDelayed( new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                public void run() {
                    Log.i(TAG,"Processing notifyRemoteVideoOnPlayingWithPostMessage");
                    mIsInPipMode = MainActivity.this.isInPictureInPictureMode();
                    if(mIsInPipMode == true) {
                        if(mIsOpenSound == true) {
                            MainActivity.this.controlSpeakerMuted(false);
                        } else {
                            MainActivity.this.controlSpeakerMuted(true);
                        }
                    } else {
                        new AlertDialog.Builder(mContext)
                                .setTitle("溫馨小提醒")
                                .setMessage(message)
                                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Continue with delete operation
                                    }
                                })
                                .show();
                    }
                }
            }, 1000);
        }

        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void requestReload() {
            new android.os.Handler(Looper.getMainLooper()).postDelayed( new Runnable() {
                public void run() {
                    String urlStr = MainActivity.sUrl;
                    MainActivity.this.loadWebrtcUrl(mSlideView, urlStr, new JSSlideInterface(MainActivity.this));
                }
            }, 1000);
        }

        @SuppressLint("JavascriptInterface")
        @JavascriptInterface
        public void postMessage(final String message){
            if(!MainActivity.this.mIsInPipMode) {
                new AlertDialog.Builder(mContext)
                        .setTitle("溫馨小提醒")
                        .setMessage(message)
                        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                            }
                        })
                        .show();
            }
        }
    }
}