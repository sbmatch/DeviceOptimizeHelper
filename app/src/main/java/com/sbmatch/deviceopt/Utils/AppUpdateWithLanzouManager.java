package com.sbmatch.deviceopt.utils;

import com.sbmatch.deviceopt.BuildConfig;
import com.sbmatch.deviceopt.bean.UpdateInfo;
import com.tencent.mmkv.MMKV;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppUpdateWithLanzouManager {
    private static final UpdateInfo updateInfo = new UpdateInfo();
    private final static String lanzouUrl = "https://giaosha.lanzoul.com";
    private final static MMKV LanzouCloud_MMKV = MMKV.mmkvWithID("lanzouCloud");
    private static final HashMap<String, Pattern> patternHashMap = new HashMap<>(){{
        put("k", Pattern.compile("var\\s+_[a-z0-9]+\\s*=\\s*'([^']*)';"));
        put("url",  Pattern.compile("url\\s*:\\s*'(.*?)'"));
        put("t", Pattern.compile("var\\s+[a-z0-9]+\\s*=\\s*'(\\d+)';"));
        put("data", Pattern.compile("data\\s*:\\s*\\{([^}]*)\\}"));
        put("fileName", Pattern.compile("<span\\s+id=\"filename\">(?:[\\s\\S]*?)</span>"));
    }};
    private AppUpdateWithLanzouManager(){

    }
    public static void Init(String url, String pwd) {
        new Thread(() -> {
            if (url.startsWith(lanzouUrl)) {

                try {
                    Elements elementsByScriptTag = Jsoup.connect(url).get().getElementsByTag("script");

                    elementsByScriptTag.forEach(script -> {
                        String scriptContent = script.html();
                        if (scriptContent != null && scriptContent.length() > 0) {

                            patternHashMap.forEach((key, p) -> {
                                Matcher matcher = p.matcher(scriptContent);
                                if (matcher.find()) {
                                    if (key.equals("data")) {
                                        LanzouCloud_MMKV.encode(key, matcher.group().split("data :")[1]);
                                    } else {
                                        LanzouCloud_MMKV.encode(key, matcher.group(1));
                                    }
                                }
                            });

                            try {
                                JSONObject jsonData = new JSONObject(LanzouCloud_MMKV.decodeString("data"));
                                jsonData.put("pg", 1);
                                jsonData.put("rep", jsonData.getInt("rep"));
                                jsonData.put("uid", jsonData.getInt("uid"));
                                jsonData.put("k", LanzouCloud_MMKV.decodeString("k"));
                                jsonData.put("t", LanzouCloud_MMKV.decodeString("t"));
                                jsonData.put("pwd", pwd);

                                Document lastFileInfo = Jsoup.connect(lanzouUrl + LanzouCloud_MMKV.decodeString("url")).requestBody(jsonToFormData(jsonData)).post();
                                setUpdateInfo(lastFileInfo.body().text());
                            } catch (JSONException | IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void setUpdateInfo(String resultData) {
        try {

            JSONObject jr = new JSONObject(resultData);

            if (jr.getInt("zt") == 1) {

                JSONObject jr2 = new JSONObject(new JSONArray(jr.getString("text")).getJSONObject(0).toString());

                updateInfo.id = jr2.getString("id");
                updateInfo.size = jr2.getString("size");
                updateInfo.update_time = jr2.getString("time");
                updateInfo.name_all = jr2.getString("name_all");
                updateInfo.version = jr2.getString("name_all").split("_")[1];

                Jsoup.connect(lanzouUrl + "/" + updateInfo.id).get().select("span.p7").forEach(desc -> {
                    String text = desc.nextSibling().nextSibling().toString().trim();
                    if (!text.startsWith("<")) {
                        updateInfo.update_desc = StringEscapeUtils.unescapeJava(text);
                    }
                });

                //MMKVHelper.get(LanzouCloud_MMKV.mmapID()).saveObject(UpdateInfo.class.getSimpleName(), updateInfo);
                LanzouCloud_MMKV.encode("updateInfo", updateInfo);

                hasUpdate();
            }
        }catch (JSONException | IOException e){
            throw new RuntimeException(e);
        }
    }

    public static UpdateInfo getUpdateInfo() {
        return updateInfo;
    }

    public static boolean hasUpdate(){
        return Integer.parseInt(updateInfo.version.replaceAll("[v.]", "")) > BuildConfig.VERSION_CODE;
    }

    public static String gotNewVersionDownloadUrl() {
       new Thread(() -> {
           try {

               Elements iframes = Jsoup.connect(lanzouUrl+ "/" + updateInfo.id).get().select("iframe.ifr2");
               iframes.forEach(iframe -> {
                   LanzouCloud_MMKV.encode("fn",  iframe.attr("src"));
               });

               Elements targetScripts = Jsoup.connect(lanzouUrl + LanzouCloud_MMKV.decodeString("fn")).get().select("script");
               targetScripts.forEach(finalScript -> {
                   String html = finalScript.html();
                   patternHashMap.forEach((key, p) -> {
                       Matcher matcher = p.matcher(html);
                       if (matcher.find()) {
                           switch (key) {
                               case "url" -> {
                                   LanzouCloud_MMKV.encode("ajax_file_url", matcher.group(1));
                               }
                               case "data" -> {
                                   MMKVHelper.get(null).saveObject(key, matcher.group().split("data :")[1]);
                                   try {
                                       // 获取原始数据
                                       String inputJsonData = MMKVHelper.get(null).getObject("data").toString();
                                       // 正则表达式匹配
                                       Pattern pattern = Pattern.compile("\\{(\\s*(\"[^\"]*\"|[^\",{}]+)\\s*:\\s*(\"[^\"]*\"|[^\",{}]+)\\s*,?\\s*)*\\}");
                                       Matcher matcher2 = pattern.matcher(StringEscapeUtils.unescapeJava(inputJsonData));

                                       if (matcher2.find()) {

                                           JSONObject file_down_jsonData = new JSONObject(matcher2.group().replaceAll("'", "\"") // 替换单引号为双引号
                                                   .replaceAll("(\\b[a-zA-Z0-9]+\b)", "\"$1\""));

                                           Document gotNewVersion = Jsoup.connect(lanzouUrl + LanzouCloud_MMKV.decodeString("ajax_file_url"))
                                                   .requestBody(jsonToFormData(file_down_jsonData))
                                                   .referrer(lanzouUrl + LanzouCloud_MMKV.decodeString("fn"))
                                                   .post();

                                           JSONObject jsonData = new JSONObject(StringEscapeUtils.unescapeJava(gotNewVersion.body().text()));
                                           if (jsonData.getInt("zt") == 1) {
                                               LanzouCloud_MMKV.encode("finalDownloadUrl", jsonData.getString("dom")+"/file/"+jsonData.getString("url"));
                                           }
                                       }
                                   } catch (IOException | JSONException ignored) {

                                   }

                               }
                           }
                       }
                   });

               });
           } catch (IOException e) {
               throw new RuntimeException(e);
           }
       }).start();

        return LanzouCloud_MMKV.decodeString("finalDownloadUrl");
    }

    private static String jsonToFormData(JSONObject jsonObj) {

        StringBuilder formData = new StringBuilder();

        // 遍历 JSON 对象的 key
        Iterator<String> it = jsonObj.keys();
        while (it.hasNext()){
            String key = it.next();
            try {
                if (formData.length() != 0) {
                    formData.append("&");
                }
                // 对 key 和 value 进行 URL 编码
                formData.append(URLEncoder.encode(key, "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(String.valueOf(jsonObj.get(key)), "UTF-8"));
            }catch (JSONException | UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        return formData.toString();
    }

}
