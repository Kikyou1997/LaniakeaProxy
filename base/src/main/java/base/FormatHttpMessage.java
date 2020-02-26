package base;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.util.internal.StringUtil;

import java.util.Map;

/**
 * @author kikyou
 */
public class FormatHttpMessage extends DefaultFullHttpResponse {

    public static HttpResponseStatus CONNECTION_ESTABLISHED = new HttpResponseStatus(200, "Connection established");

    public FormatHttpMessage(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
    }

    public FormatHttpMessage(HttpVersion version, HttpResponseStatus status, ByteBuf content) {
        super(version, status, content);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb = HttpMessageUtil.appendInitialLine(sb, this);
        sb = HttpMessageUtil.appendHeaders(sb, this.headers());
        sb = HttpMessageUtil.appendHeaders(sb, this.trailingHeaders());
        HttpMessageUtil.removeLastNewLine(sb);
        return sb.toString();
    }

    private static class HttpMessageUtil {
        private static StringBuilder appendHeaders(StringBuilder buf, HttpHeaders headers) {
            for (Map.Entry<String, String> e : headers) {
                buf.append(e.getKey());
                buf.append(": ");
                buf.append(e.getValue());
                buf.append(StringUtil.CARRIAGE_RETURN);
                buf.append(StringUtil.NEWLINE);
            }
            return buf;
        }

        private static StringBuilder appendInitialLine(StringBuilder buf, HttpResponse res) {
            buf.append(res.protocolVersion());
            buf.append(' ');
            buf.append(res.status());
            buf.append(StringUtil.NEWLINE);
            return buf;
        }


        private static void removeLastNewLine(StringBuilder buf) {
            buf.setLength(buf.length() - StringUtil.NEWLINE.length());
        }
    }
}
