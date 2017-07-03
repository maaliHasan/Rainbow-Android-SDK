package net.java.sip.communicator.impl.protocol.jabber.extensions;

import android.util.Log;

import com.ale.util.StringsUtil;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidatePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.CandidateType;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.DtlsFingerprintPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.GroupPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.IceUdpTransportPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleAction;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ParameterPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.PayloadTypePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RTPHdrExtPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtcpFbPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtcpMuxExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.RtpDescriptionPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.SourcePacketExtension;

import org.jivesoftware.smack.packet.IQ;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by boris on 27/01/15.
 */
public class JingleUtils
{
    private static final String NL = "\n";
    private static String s_lastUfrag;
    private static String s_lastPassword;

    public static SessionDescription toSdp(JingleIQ iq, String type)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("v=0").append(NL);
        sb.append("o=- 1923518516 2 IN IP4 127.0.0.1").append(NL);
        sb.append("s=-").append(NL);
        sb.append("t=0 0").append(NL);
        sb.append("a=group:BUNDLE");

        for (ContentPacketExtension cpe : iq.getContentList())
        {
            sb.append(" ");
            sb.append(cpe.getName());
        }

        sb.append(NL);

        sb.append("a=ice_options:trickle").append(NL);

        for (ContentPacketExtension cpe : iq.getContentList())
        {
            RtpDescriptionPacketExtension description = cpe.getFirstChildOfType(RtpDescriptionPacketExtension.class);

            if (description != null)
            {
                for (SourcePacketExtension ssrc : description.getChildExtensionsOfType(SourcePacketExtension.class))
                {
                    for (ParameterPacketExtension param : ssrc.getParameters())
                    {
                        if ("mslabel".equals(param.getName()))
                        {
                            StringBuilder temp = new StringBuilder();
                            temp.append("a=msid-semantic:").append(" ").append("WMS").append(" ").append(param.getValue()).append(NL);

                            if (!sb.toString().contains(temp))
                                sb.append(temp);
                        }
                    }
                }
            }
        }

        for (ContentPacketExtension cpe : iq.getContentList())
        {
            appendMLine(cpe, sb);
        }

        return new SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sb.toString());
    }

    private static void appendMLine(ContentPacketExtension cpe, StringBuilder sb)
    {
        RtpDescriptionPacketExtension description = cpe.getFirstChildOfType(RtpDescriptionPacketExtension.class);

        IceUdpTransportPacketExtension transport = cpe.getFirstChildOfType(IceUdpTransportPacketExtension.class);

        DtlsFingerprintPacketExtension dtls = transport.getFirstChildOfType(DtlsFingerprintPacketExtension.class);

        sb.append("m=").append(cpe.getName()).append(" 9 UDP/TLS/RTP/SAVPF");

        if (description != null)
        {
            for (PayloadTypePacketExtension pt : description.getPayloadTypes())
            {
                sb.append(" ").append(pt.getID());
            }
        }

        sb.append(NL);

        sb.append("c=IN IP4 0.0.0.0").append(NL);
        sb.append("a=rtcp:1 IN IP4 0.0.0.0").append(NL);

        for (CandidatePacketExtension candidate : transport.getCandidateList())
        {
            getCandidateFromPacketExtension(sb, transport, candidate, "a=");
        }

        sb.append("a=ice-ufrag:").append(transport.getUfrag()).append(NL);
        sb.append("a=ice-pwd:").append(transport.getPassword()).append(NL);

        if (dtls != null)
        {
            sb.append("a=fingerprint:").append(dtls.getHash()).append(' ').append(dtls.getFingerprint()).append(NL);
            sb.append("a=setup:").append(dtls.getSetup()).append(NL);
        }
        sb.append("a=mid:").append(cpe.getName()).append(NL); // XXX cpe.getName or description.getMedia()?

        if (description != null)
        {
            for (RTPHdrExtPacketExtension ext : description.getExtmapList())
            {
                sb.append("a=extmap:").append(ext.getID()).append(' ').append(ext.getURI()).append(NL);
            }
        }

        sb.append("a=sendrecv").append(NL);
        sb.append("a=rtcp-mux").append(NL);

        if (description != null && description.getChildExtensionsOfType(SourcePacketExtension.class).size() > 1)
        {
            sb.append("a=rtcp-rsize").append(NL);
        }

        if (description != null)
        {
            for (PayloadTypePacketExtension pt : description.getPayloadTypes())
            {
                sb.append("a=rtpmap:").append(pt.getID()).append(' ').append(pt.getName()).append('/').append(pt.getClockrate());
                if (pt.getChannels() != 1)
                    sb.append('/').append(pt.getChannels());
                sb.append(NL);

                for (ParameterPacketExtension ppe : pt.getChildExtensionsOfType(ParameterPacketExtension.class))
                    sb.append("a=fmtp:").append(pt.getID()).append(' ').append(ppe.getName()).append('=').append(ppe.getValue()).append(NL);

                for (RtcpFbPacketExtension fb : pt.getChildExtensionsOfType(RtcpFbPacketExtension.class))
                    sb.append("a=rtcp-fb:").append(pt.getID()).append(' ').append(fb.getFeedbackType()).append(NL);
            }

            if (description.getChildExtensionsOfType(SourcePacketExtension.class).size() > 1)
            {
                sb.append("a=ssrc-group:FID");

                for (SourcePacketExtension ssrc : description.getChildExtensionsOfType(SourcePacketExtension.class))
                {
                    sb.append(" ").append(ssrc.getSSRC());
                }

                sb.append(NL);
            }

            for (SourcePacketExtension ssrc : description.getChildExtensionsOfType(SourcePacketExtension.class))
            {
                long ssrcL = ssrc.getSSRC();
                for (ParameterPacketExtension param : ssrc.getParameters())
                {
                    sb.append("a=ssrc:").append(ssrcL).append(" ").append(param.getName()).append(":").append(param.getValue()).append(NL);
                }
            }
        }
    }

    private static void getCandidateFromPacketExtension(StringBuilder sb, IceUdpTransportPacketExtension transport, CandidatePacketExtension candidate, String prefix)
    {
        sb.append(prefix).append("candidate:").append(candidate.getFoundation()).append(' ').append(candidate.getComponent());
        sb.append(' ').append(candidate.getProtocol()).append(' ').append(candidate.getPriority());
        sb.append(' ').append(candidate.getIP()).append(' ').append(candidate.getPort()).append(" typ ");
        sb.append(candidate.getType().toString());

        if (candidate.getRelAddr() != null)
        {
            sb.append(" raddr ").append(candidate.getRelAddr());
            sb.append(" rport ").append(candidate.getRelPort());
        }

        sb.append(" generation ").append(candidate.getGeneration());

        if (!StringsUtil.isNullOrEmpty(transport.getUfrag()))
            sb.append(" ufrag ").append(transport.getUfrag());

        if (!StringsUtil.isNullOrEmpty(transport.getPassword()))
            sb.append(" pwd ").append(transport.getPassword());

        if (candidate.getNetwork() != -1)
            sb.append(" network-id ").append(candidate.getNetwork());

        if (candidate.getCost() != -1)
            sb.append(" network-cost ").append(candidate.getCost());

        sb.append(NL);
    }

    public static String toCandidateString(JingleIQ iq)
    {
        StringBuilder sb = new StringBuilder();

        for (ContentPacketExtension cpe : iq.getContentList())
        {
            IceUdpTransportPacketExtension transport = cpe.getFirstChildOfType(IceUdpTransportPacketExtension.class);

            for (CandidatePacketExtension candidate : transport.getCandidateList())
            {
                getCandidateFromPacketExtension(sb, transport, candidate, StringsUtil.EMPTY);
            }
        }

        return sb.toString();
    }

    public static JingleIQ toJingle(SessionDescription sdp, JingleAction action, ContentPacketExtension.CreatorEnum creator)
    {
        JingleIQ iq = new JingleIQ();
        iq.setAction(action);

        GroupPacketExtension groupExtension = new GroupPacketExtension();
        groupExtension.setSemantics("BUNDLE");
        iq.setGroup(groupExtension);

        List<ContentPacketExtension> contents = new ArrayList<>();

        List<String> medias = getMedias(sdp);

        for (String media : medias)
        {
            ContentPacketExtension groupContent = new ContentPacketExtension();
            groupContent.setName(media);
            contents.add(groupContent);

            ContentPacketExtension content = createContentForMedia(media, sdp, creator);
            iq.addContent(content);
        }


        groupExtension.addContents(contents);

        return iq;
    }

    private static ContentPacketExtension createContentForMedia(String mediaType, SessionDescription sdp, ContentPacketExtension.CreatorEnum creator)
    {
        ContentPacketExtension content = new ContentPacketExtension(creator, mediaType);

        content.setSenders(ContentPacketExtension.SendersEnum.both);

        RtpDescriptionPacketExtension rtpDesc = new RtpDescriptionPacketExtension();

        rtpDesc.setMedia(mediaType);

        for (String line : getExtmapLines(mediaType, sdp))
        {
            RTPHdrExtPacketExtension rtpHdrExtPacketExtension = new RTPHdrExtPacketExtension();
            rtpHdrExtPacketExtension.setID(line.split(":")[1].split(" ")[0]);
            rtpHdrExtPacketExtension.setURI(URI.create(line.split(" ")[1].trim()));
            rtpDesc.addExtmap(rtpHdrExtPacketExtension);
        }

        for (String line : getRtpmapLines(mediaType, sdp))
        {
            String payload = line.split(":")[1].trim();
            String id = payload.split(" ")[0];
            String[] payloadParams = payload.split(" ")[1].split("/");

            PayloadTypePacketExtension payloadTypePacketExtension = new PayloadTypePacketExtension();
            payloadTypePacketExtension.setId(Integer.parseInt(id));
            payloadTypePacketExtension.setName(payloadParams[0]);
            payloadTypePacketExtension.setClockrate(Integer.parseInt(payloadParams[1]));

            if (payloadParams.length > 2)
                payloadTypePacketExtension.setChannels(Integer.parseInt(payloadParams[2]));

            for (String paramLine : getFmtpLines(mediaType, id, sdp))
            {
                String paramPacketExtensions = paramLine.split(" ")[1].trim();

                for (String paramPacketExtension : paramPacketExtensions.split(";"))
                {
                    String[] keyValue = paramPacketExtension.split("=");

                    ParameterPacketExtension parameterPacketExtension = new ParameterPacketExtension();
                    parameterPacketExtension.setName(keyValue[0]);
                    parameterPacketExtension.setValue(keyValue[1]);
                    payloadTypePacketExtension.addParameter(parameterPacketExtension);
                }
            }

            for (String rtcpfbLine : getRtcpFbLines(mediaType, id, sdp))
            {
                RtcpFbPacketExtension rtcpFbPacketExtension = new RtcpFbPacketExtension();
                rtcpFbPacketExtension.setFeedbackType(rtcpfbLine.split(" ")[1].trim());
                payloadTypePacketExtension.addRtcpFeedbackType(rtcpFbPacketExtension);
            }

            rtpDesc.addPayloadType(payloadTypePacketExtension);
        }

        content.addChildExtension(rtpDesc);

        List<String> cname = new ArrayList<>(), label = new ArrayList<>(), msid = new ArrayList<>(), mslabel = new ArrayList<>(), ssrc = new ArrayList<>();

        for (String line : getMediaSsrcLines(mediaType, sdp))
        {
            if (!ssrc.contains(line.split(" ")[0].split(":")[1]))
                ssrc.add(line.split(" ")[0].split(":")[1]);

            String k = line.split(" ")[1].split(":")[0];
            if ("cname".equals(k))
            {
                cname.add(line.split(" ")[1].split(":")[1].trim());
            }
            else if ("label".equals(k))
            {
                label.add(line.split(" ")[1].split(":")[1].trim());
            }
            else if ("msid".equals(k))
            {
                msid.add(line.split(" ")[1].split(":")[1].trim());
            }
            else if ("mslabel".equals(k))
            {
                mslabel.add(line.split(" ")[1].split(":")[1].trim());
            }
        }

        for (int i = 0; i < ssrc.size(); i++)
        {
            SourcePacketExtension spe = new SourcePacketExtension();

            try
            {
                Long value = Long.valueOf(ssrc.get(i));
                spe.setSSRC(value);
                rtpDesc.setSsrc(ssrc.get(0));
            }
            catch (Exception e)
            {
                Log.e("error", "Parsing problem", e);
            }

            ParameterPacketExtension p = new ParameterPacketExtension();

            p.setName("cname");
            p.setValue(cname.get(i));
            spe.addParameter(p);

            p = new ParameterPacketExtension();
            p.setName("label");
            p.setValue(label.get(i));
            spe.addParameter(p);

            p = new ParameterPacketExtension();
            p.setName("msid");
            p.setValue(msid.get(i) + " " + label.get(i).trim());
            spe.addParameter(p);

            p = new ParameterPacketExtension();
            p.setName("mslabel");
            p.setValue(mslabel.get(i));
            spe.addParameter(p);

            rtpDesc.addChildExtension(spe);
        }

        rtpDesc.addChildExtension(new RtcpMuxExtension());

        IceUdpTransportPacketExtension transport = new IceUdpTransportPacketExtension();

        if (mediaType.equals("audio"))
        {
            for (String line : getCandidateLines(sdp))
            {
                transport.addCandidate(createCandidateExtension(line));
            }
        }

        String fp = null;
        s_lastUfrag = null;
        s_lastPassword = null;

        for (String line : getMediaIceLines(mediaType, sdp))
        {
            if (line.contains("frag"))
                s_lastUfrag = line.split(":")[1].trim();
            else if (line.contains("pwd"))
                s_lastPassword = line.split(":")[1].trim();
            else if (line.contains("nger"))
                fp = line.split(" ")[1].trim();
        }
        transport.setPassword(s_lastPassword);
        transport.setUfrag(s_lastUfrag);
        DtlsFingerprintPacketExtension f = new DtlsFingerprintPacketExtension();
        f.setFingerprint(fp);
        f.setHash("sha-256");
        f.setSetup(getSetup(sdp));
        transport.addChildExtension(f);
        content.addChildExtension(transport);

        return content;
    }

    public static JingleIQ createTransportInfo(String jid, IceCandidate candidate, ContentPacketExtension.CreatorEnum creator)
    {
        JingleIQ iq = new JingleIQ();
        iq.setAction(JingleAction.TRANSPORT_INFO);
        iq.setTo(jid);
        iq.setType(IQ.Type.set);

        ContentPacketExtension content = new ContentPacketExtension(creator, candidate.sdpMid);
        IceUdpTransportPacketExtension transport = new IceUdpTransportPacketExtension();

        transport.setUfrag(s_lastUfrag);
        transport.setPassword(s_lastPassword);
        transport.addCandidate(createCandidateExtension(candidate.sdp));
        content.addChildExtension(transport);
        iq.addContent(content);

        return iq;
    }

    private static CandidatePacketExtension createCandidateExtension(String c)
    {
        String candidate = c.replace("\r", "");

        CandidatePacketExtension cpe = new CandidatePacketExtension();

        String foundation = (candidate.split(":")[1]).split(" ")[0];
        String component = candidate.split(" ")[1];
        String protocol = candidate.split(" ")[2];
        String priority = candidate.split(" ")[3];
        String addr = candidate.split(" ")[4];
        String port = candidate.split(" ")[5];
        String typ = candidate.split(" ")[7];

        cpe.setPort(Integer.valueOf(port));
        cpe.setFoundation(foundation);
        cpe.setProtocol(protocol);
        cpe.setPriority(Long.valueOf(priority));
        cpe.setComponent(Integer.valueOf(component));
        cpe.setIP(addr);

        if ("host".equals(typ))
            cpe.setType(CandidateType.host);
        else if ("prflx".equals(typ))
            cpe.setType(CandidateType.prflx);
        else if ("relay".equals(typ))
            cpe.setType(CandidateType.relay);
        else if ("srflx".equals(typ))
            cpe.setType(CandidateType.srflx);


        for (int i = 0; i < candidate.split(" ").length; i++)
        {
            if (candidate.split(" ")[i].equals("raddr"))
                cpe.setRelAddr(candidate.split(" ")[i + 1]);
            else if (candidate.split(" ")[i].equals("rport"))
                cpe.setRelPort(Integer.valueOf(candidate.split(" ")[i + 1]));
            else if (candidate.split(" ")[i].equals("generation"))
                cpe.setGeneration(Integer.valueOf(candidate.split(" ")[i + 1]));
            else if (candidate.split(" ")[i].equals("network-id"))
                cpe.setNetwork(Integer.valueOf(candidate.split(" ")[i + 1]));
            else if (candidate.split(" ")[i].equals("network-cost"))
                cpe.setCost(Integer.valueOf(candidate.split(" ")[i + 1]));
        }

        return cpe;
    }

    private static String getSetup(SessionDescription sdp)
    {
        String[] lines = sdp.description.split("\r\n");
        Log.i("some", "SDP LINES: " + lines.length);
        String setup = null;

        for (String s : lines)
        {
            if (s.startsWith("a=setup:"))
            {
                setup = s.substring("a=setup:".length());
                break;
            }
        }

        return setup;
    }

    private static List<String> getMediaSsrcLines(String mediaType, SessionDescription sdp)
    {
        String[] lines = sdp.description.split("\n");
        Log.i("some", "SDP LINES: " + lines.length);
        LinkedList<String> ret = new LinkedList<>();

        boolean in = false;
        for (String s : lines)
        {
            if (s.startsWith("m=" + mediaType))
            {
                in = true;
                continue;
            }
            if (!in)
                continue;
            if (s.startsWith("m="))
                return ret;
            if (s.startsWith("a=ssrc:"))
                ret.add(s);
        }

        return ret;

    }

    private static List<String> getExtmapLines(String mediaType, SessionDescription sdp)
    {
        String[] lines = sdp.description.split("\n");
        Log.i("some", "SDP LINES: " + lines.length);
        LinkedList<String> ret = new LinkedList<>();

        boolean in = false;
        for (String s : lines)
        {
            if (s.startsWith("m=" + mediaType))
            {
                in = true;
                continue;
            }
            if (!in)
                continue;
            if (s.startsWith("m="))
                return ret;
            if (s.startsWith("a=extmap:"))
                ret.add(s);
        }

        return ret;

    }

    private static List<String> getRtpmapLines(String mediaType, SessionDescription sdp)
    {
        String[] lines = sdp.description.split("\n");
        Log.i("some", "SDP LINES: " + lines.length);
        LinkedList<String> ret = new LinkedList<>();

        boolean in = false;
        for (String s : lines)
        {
            if (s.startsWith("m=" + mediaType))
            {
                in = true;
                continue;
            }
            if (!in)
                continue;
            if (s.startsWith("m="))
                return ret;
            if (s.startsWith("a=rtpmap:"))
                ret.add(s);
        }

        return ret;

    }

    private static List<String> getFmtpLines(String mediaType, String id, SessionDescription sdp)
    {
        String[] lines = sdp.description.split("\n");
        Log.i("some", "SDP LINES: " + lines.length);
        LinkedList<String> ret = new LinkedList<>();

        boolean in = false;
        for (String s : lines)
        {
            if (s.startsWith("m=" + mediaType))
            {
                in = true;
                continue;
            }
            if (!in)
                continue;
            if (s.startsWith("m="))
                return ret;
            if (s.startsWith("a=fmtp:" + id))
                ret.add(s);
        }

        return ret;

    }

    private static List<String> getRtcpFbLines(String mediaType, String id, SessionDescription sdp)
    {
        String[] lines = sdp.description.split("\n");
        Log.i("some", "SDP LINES: " + lines.length);
        LinkedList<String> ret = new LinkedList<>();

        boolean in = false;
        for (String s : lines)
        {
            if (s.startsWith("m=" + mediaType))
            {
                in = true;
                continue;
            }
            if (!in)
                continue;
            if (s.startsWith("m="))
                return ret;
            if (s.startsWith("a=rtcp-fb:" + id))
                ret.add(s);
        }

        return ret;

    }

    private static List<String> getMediaIceLines(String mediaType, SessionDescription sdp)
    {
        String[] lines = sdp.description.split("\n");
        Log.i("some", "SDP LINES: " + lines.length);
        LinkedList<String> ret = new LinkedList<>();

        boolean in = false;
        for (String s : lines)
        {
            if (s.startsWith("m=" + mediaType))
            {
                in = true;
                continue;
            }
            if (!in)
                continue;
            if (s.startsWith("m="))
                return ret;
            if (s.startsWith("a=ice") || s.startsWith("a=finge"))
                ret.add(s);
        }

        return ret;

    }

    private static List<String> getMedias(SessionDescription sdp)
    {
        String[] lines = sdp.description.split("\n");
        Log.i("some", "SDP LINES: " + lines.length);
        LinkedList<String> ret = new LinkedList<>();

        for (String s : lines)
        {
            if (s.startsWith("m="))
                ret.add(s.split(" ")[0].substring(2));
        }

        return ret;

    }

    private static List<String> getCandidateLines(SessionDescription sdp)
    {
        String[] lines = sdp.description.split("\n");
        Log.i("some", "SDP LINES: " + lines.length);
        LinkedList<String> ret = new LinkedList<>();

        for (String s : lines)
        {
            if (s.startsWith("a=candidate"))
            {
                ret.add(s);
            }
        }

        return ret;

    }

    public static int getMediaIndex(SessionDescription sdp, String media)
    {
        if (sdp != null)
        {
            List<String> medias = getMedias(sdp);

            for (int i = 0; i < medias.size(); i++)
            {
                if (medias.get(i).equals(media))
                    return i;
            }
        }

        return -1;
    }
}
