package com.mateo.estrucdinami;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.view.View;
import android.view.Window;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/** EstrucDinami: muestra la app de estudio (web/index.html) a pantalla completa. */
public class MainActivity extends Activity {

    private static final int REQ_FILE = 41;
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;

    /** Ajustes que se aplican a la página al terminar de cargar. */
    private static final String HOOKS =
            "(function(){if(window.__estrucdinami)return;window.__estrucdinami=1;"
            // Imprimir / guardar PDF con el sistema de impresión de Android
            + "window.print=function(){AndroidApp.print();};"
            // Copiar al portapapeles del teléfono
            + "var w=function(t){AndroidApp.copy(String(t));return Promise.resolve();};"
            + "try{if(navigator.clipboard){navigator.clipboard.writeText=w;}"
            + "else{Object.defineProperty(navigator,'clipboard',{value:{writeText:w},configurable:true});}}catch(e){}"
            // Barras del sistema del mismo color que el tema (claro u oscuro)
            + "function bars(){var t=document.documentElement.getAttribute('data-theme');"
            + "var d=t?t==='dark':matchMedia('(prefers-color-scheme: dark)').matches;AndroidApp.setDark(d);}"
            + "bars();new MutationObserver(bars).observe(document.documentElement,{attributes:true,attributeFilter:['data-theme']});"
            + "try{matchMedia('(prefers-color-scheme: dark)').addEventListener('change',bars);}catch(e){}"
            + "})();";

    /** Botón atrás: cierra un cuadro de diálogo o vuelve a Inicio; si ya está en Inicio, sale. */
    private static final String BACK =
            "(function(){var d=document.querySelector('.dialog');if(d){d.remove();return 'ok';}"
            + "var h=(location.hash||'').replace('#','');"
            + "if(h&&h!=='inicio'){location.hash='inicio';return 'ok';}return 'exit';})()";

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        web = new WebView(this);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if ("file".equals(u.getScheme())) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, u));
                } catch (Exception ignored) {
                }
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                view.evaluateJavascript(HOOKS, null);
            }
        });

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                // «Cargar archivo .json» de la copia de seguridad
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT)
                        .addCategory(Intent.CATEGORY_OPENABLE)
                        .setType("*/*");
                try {
                    startActivityForResult(Intent.createChooser(i, "Elegir copia de seguridad"), REQ_FILE);
                } catch (Exception e) {
                    fileCallback = null;
                    return false;
                }
                return true;
            }
        });

        web.addJavascriptInterface(new Bridge(), "AndroidApp");
        web.loadUrl("file:///android_asset/index.html");
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_FILE && fileCallback != null) {
            fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            fileCallback = null;
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        web.evaluateJavascript(BACK, result -> {
            if (result == null || result.contains("exit")) moveTaskToBack(true);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        web.onResume();
    }

    @Override
    protected void onPause() {
        web.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        web.removeJavascriptInterface("AndroidApp");
        web.destroy();
        super.onDestroy();
    }

    /** Funciones que la página puede llamar como window.AndroidApp.*. */
    public class Bridge {
        @JavascriptInterface
        public void print() {
            runOnUiThread(() -> {
                PrintManager pm = getSystemService(PrintManager.class);
                if (pm == null) return;
                String name = "EstrucDinami - banco de preguntas";
                pm.print(name, web.createPrintDocumentAdapter(name), new PrintAttributes.Builder().build());
            });
        }

        @JavascriptInterface
        public void copy(final String text) {
            runOnUiThread(() -> {
                ClipboardManager cm = getSystemService(ClipboardManager.class);
                if (cm != null) {
                    cm.setPrimaryClip(ClipData.newPlainText("EstrucDinami", text));
                    Toast.makeText(MainActivity.this, "Copiado al portapapeles", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @SuppressWarnings("deprecation")
        @JavascriptInterface
        public void setDark(final boolean dark) {
            runOnUiThread(() -> {
                Window w = getWindow();
                int color = dark ? 0xFF0D1119 : 0xFFEDF0F4;
                w.setStatusBarColor(color);
                w.setNavigationBarColor(color);
                View decor = w.getDecorView();
                int flags = decor.getSystemUiVisibility();
                int light = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                decor.setSystemUiVisibility(dark ? (flags & ~light) : (flags | light));
                web.setBackgroundColor(color);
            });
        }
    }
}
