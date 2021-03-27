//package top.ivan.windrop.ctl;
//
//import com.alibaba.fastjson.JSON;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.server.ServerWebExchange;
//import top.ivan.windrop.WinDropApplication;
//import top.ivan.windrop.bean.ApplyRequest;
//import top.ivan.windrop.bean.ApplyResponse;
//import top.ivan.windrop.bean.WindropConfig;
//import top.ivan.windrop.clip.ClipBean;
//import top.ivan.windrop.clip.FileClipBean;
//import top.ivan.windrop.clip.ImageClipBean;
//import top.ivan.windrop.clip.TextClipBean;
//import top.ivan.windrop.svc.ApplyKeyService;
//import top.ivan.windrop.svc.ClipSvc;
//import top.ivan.windrop.svc.IPVerifier;
//
//import java.net.InetSocketAddress;
//
///**
// * @author Ivan
// * @description
// * @date 2021/3/12
// */
//@Slf4j
////@RestController
////@RequestMapping("${windrop.prefix:/windrop}")
//public class ApplyController {
//
//    @Autowired
//    private IPVerifier ipVerifier;
//
//    @Autowired
//    private WindropConfig config;
//
//    @Autowired
//    private ClipSvc clipSvc;
//
//    @Autowired
//    private ApplyKeyService keyService;
//
//
//    @PostMapping("apply")
//    public ResponseEntity<?> apply(@RequestBody ApplyRequest request, ServerWebExchange exchange) {
//        InetSocketAddress address = exchange.getRequest().getRemoteAddress();
//        log.debug("receive apply request from '{}'", exchange.getRequest().getRemoteAddress());
//
//        String ip = address.getAddress().getHostAddress();
//        if (!ipVerifier.accessible(ip)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApplyResponse.failed("无权限访问"));
//        }
//        if (!confirm(ip, request)) {
//            return ResponseEntity.ok().body(ApplyResponse.failed("请求已被取消"));
//        }
//        String accessKey = keyService.getKey();
//        log.debug("apply for accessKey: {}", accessKey);
//        return ResponseEntity.ok(ApplyResponse.success(accessKey));
//    }
//
//    private boolean confirm(String ip, ApplyRequest request) {
//        String type = request.getType();
//        if (config.needConfirm(type)) {
//            String msg;
//            switch (type) {
//                case "pull":
//                    msg = "推送'" + getClipBeanTypeName() + "'到设备?";
//                    break;
//                case "file":
//                case "image":
//                    msg = "是否接收文件/图片: " + request.getFilename() + "（" + request.getSize() + ")?";
//                    break;
//                case "text":
//                    msg = "是否接收文本?";
//                    break;
//                default:
//                    msg = "未定义请求: " + JSON.toJSONString(request);
//                    break;
//            }
//            return WinDropApplication.WindropHandler.confirm("来自" + ip, msg);
//        }
//        return true;
//    }
//
//    private String getClipBeanTypeName() {
//        ClipBean clipBean = clipSvc.getClipBean();
//        if (clipBean instanceof FileClipBean) {
//            return "文件";
//        } else if (clipBean instanceof ImageClipBean) {
//            return "图片";
//        } else if (clipBean instanceof TextClipBean) {
//            return "文本";
//        }
//        return "未知类型";
//    }
//
//
//}
