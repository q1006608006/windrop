package test;

import top.ivan.windrop.util.JSONUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Ivan
 * @since 2023/11/09 10:31
 */
public class MockAPI {
    public class ModelA {
    }

    public class ModelB {
    }

    public static class ModelResponse {

        public static ModelResponse of(Object any) {
            return new ModelResponse();
        }
    }

    public class JudgeResponse {
        int status = 200;
    }

    private static Executor executor = Executors.newCachedThreadPool();

    public ModelResponse mockRequest(ModelA req, ModelB req2) {
        //你们的业务逻辑
        //...
        int type = (int) (Math.random() * 100) % 5;

        JudgeResponse result = null;
        switch (type) {
            case 2:
                //模型决策
                result = judge(req, req2);
                break;
            case 3:
                //打分卡
                break;
            case 4:
                //模型决策
                result = judge(req, req2);
                //调融合分，决策立即返回，但是没结果，需要提供一个回调地址
                asyncJudgeForMix(req, req2, "acceptMix", this::acceptMix);
            default:
                break;
        }
        return ModelResponse.of(result);
    }

    public JudgeResponse requestJudge(String url, Object params, boolean sync, Consumer<JudgeResponse> consumer) {
        Function<Object, JudgeResponse> supplier = map -> {
            JudgeResponse res = new JudgeResponse();
            res.status = 200;
            return res;
        };

        if (sync) {
            return supplier.apply(params);
        } else {
            //这里只是模拟异步的场景
            executor.execute(() -> {
                JudgeResponse res = supplier.apply(params);
                consumer.accept(res);
            });

            JudgeResponse res = new JudgeResponse();
            res.status = 202;
            return res;
        }
    }


    private JudgeResponse judge(ModelA a, ModelB b) {
        JudgeResponse response = requestJudge("syncUrl", a, true, null);
        //同步
        assert response.status == 200;
        return response;
    }

    private void asyncJudgeForMix(ModelA a, ModelB b, String callBack, Consumer<JudgeResponse> consumer) {
        JudgeResponse async = requestJudge("asyncUrl?callBack=" + callBack, b, false, consumer);
        //异步
        assert async.status == 202;
    }

    private void acceptMix(JudgeResponse mix) {
        //你们的融合分逻辑
        System.out.println(JSONUtils.toString(mix));
    }

}
