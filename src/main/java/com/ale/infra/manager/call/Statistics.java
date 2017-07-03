package com.ale.infra.manager.call;

import org.webrtc.StatsReport;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cebruckn on 15/05/2017.
 */

public class Statistics
{
    private static final int MAX_VALUES = 3;
    
    private static final String AUDIO_RTT_ON_SENT = "audioRttOnSent";
    private static final String AUDIO_PACKETS_LOST_ON_SENT = "audioPacketsLostOnSent";
    private static final String AUDIO_JITTER_ON_RECV = "audioJitterOnRecv";
    private static final String AUDIO_PACKETS_LOST_ON_RECV = "audioPacketsLostOnRecv";
    private static final String VIDEO_RTT_ON_SENT = "videoRttOnSent";
    private static final String VIDEO_PACKETS_LOST_ON_SENT = "videoPacketsLostOnSent";
    private static final String VIDEO_JITTER_ON_RECV = "videoJitterOnRecv";
    private static final String VIDEO_PACKETS_LOST_ON_RECV = "videoPacketsLostOnRecv";

    private static Map<String, int[]> s_high;
    private static Map<String, int[]> s_medium;

    private Map<String, Integer> m_data = new HashMap<>();

    private int[] m_audioSentStreamRtt = new int[MAX_VALUES];
    private int[] m_audioReceiveStreamJitter = new int[MAX_VALUES];
    private int[] m_videoSentStreamRtt = new int[MAX_VALUES];
    private int[] m_videoReceiveStreamJitter = new int[MAX_VALUES];

    private int m_audioSentStreamRttIndex = 0;
    private int m_audioReceiveStreamJitterIndex = 0;
    private int m_videoSentStreamRttIndex = 0;
    private int m_videoReceiveStreamJitterIndex = 0;

    private int m_previousAudioPacketsSent = 0;
    private int m_previousAudioPacketsReceived = 0;
    private int m_previousVideoPacketsSent = 0;
    private int m_previousVideoPacketsReceived = 0;

    private int m_previousAudioPacketsSentLost = 0;
    private int m_previousAudioPacketsReceivedLost = 0;
    private int m_previousVideoPacketsSentLost = 0;
    private int m_previousVideoPacketsReceivedLost = 0;

    private static Map<String, int[]> high()
    {
        if (s_high == null)
        {
            s_high = new HashMap<>();

            s_high.put(AUDIO_RTT_ON_SENT, new int[]{250, 250, 250});
            s_high.put(AUDIO_PACKETS_LOST_ON_SENT, new int[]{2, 2, 2});
            s_high.put(AUDIO_JITTER_ON_RECV, new int[]{100, 100, 100});
            s_high.put(AUDIO_PACKETS_LOST_ON_RECV, new int[]{2, 2, 2});
            s_high.put(VIDEO_RTT_ON_SENT, new int[]{50, 100, 200});
            s_high.put(VIDEO_PACKETS_LOST_ON_SENT, new int[]{3, 2, 1});
            s_high.put(VIDEO_JITTER_ON_RECV, new int[]{100, 100, 100});
            s_high.put(VIDEO_PACKETS_LOST_ON_RECV, new int[]{3, 2, 1});
        }

        return s_high;
    }

    private static Map<String, int[]> medium()
    {
        if (s_medium == null)
        {
            s_medium = new HashMap<>();

            s_medium.put(AUDIO_RTT_ON_SENT, new int[]{1000, 1000, 1000, 1000, 1000});
            s_medium.put(AUDIO_PACKETS_LOST_ON_SENT, new int[]{5, 5, 5, 5, 5});
            s_medium.put(AUDIO_JITTER_ON_RECV, new int[]{200, 200, 200, 200, 200});
            s_medium.put(AUDIO_PACKETS_LOST_ON_RECV, new int[]{5, 5, 5, 5, 5});
            s_medium.put(VIDEO_RTT_ON_SENT, new int[]{100, 150, 200, 300, 500});
            s_medium.put(VIDEO_PACKETS_LOST_ON_SENT, new int[]{8, 4, 3, 2, 1});
            s_medium.put(VIDEO_JITTER_ON_RECV, new int[]{200, 200, 200, 200, 200});
            s_medium.put(VIDEO_PACKETS_LOST_ON_RECV, new int[]{8, 4, 3, 2, 1});

        }

        return s_medium;
    }

    private boolean isSpecCompliant(Map<String, int[]> qualitySpecifications)
    {
        boolean isValid = true;
        String[] validDataLabel = m_data.keySet().toArray(new String[m_data.keySet().size()]);

        if (validDataLabel.length == 0)
            return false;

        String property = validDataLabel[0];
        for (int index = 0; index < qualitySpecifications.get(property).length; index++)
        {
            if (m_data.get(property) <= qualitySpecifications.get(property)[index])
            {
                for (String attribute : validDataLabel)
                {
                    if (m_data.get(attribute) > qualitySpecifications.get(attribute)[index])
                    {
                        isValid = false;
                        break;
                    }
                }
                if (isValid)
                    return true;

                isValid = true;
            }
        }

        return false;
    }

    public Quality extractQualityIndicatorFromStats(StatsReport[] reports)
    {
        for (StatsReport report : reports)
        {
            if (report.type.equals("ssrc") && report.id.contains("ssrc"))
            {
                Map<String, String> reportMap = getReportMap(report);
                String media = reportMap.get("mediaType");
                String packetsLost = reportMap.get("packetsLost");

                if (report.id.contains("send"))
                {
                    String packetsSent = reportMap.get("packetsSent");
                    String googRtt = reportMap.get("googRtt");

                    if ("video".equals(media))
                    {
                        if (googRtt != null)
                        {
                            m_videoSentStreamRtt[m_videoSentStreamRttIndex % MAX_VALUES] = Integer.parseInt(googRtt);
                            m_videoSentStreamRttIndex++;
                        }

                        if (packetsLost != null && packetsSent != null)
                        {
                            if ((Integer.parseInt(packetsSent) - m_previousVideoPacketsSent) == 0)
                                m_data.put(VIDEO_PACKETS_LOST_ON_SENT, 0);
                            else
                                m_data.put(VIDEO_PACKETS_LOST_ON_SENT, ((Integer.parseInt(packetsLost) - m_previousVideoPacketsSentLost) * 100) / (Integer.parseInt(packetsSent) - m_previousVideoPacketsSent));

                            m_previousVideoPacketsSent = Integer.parseInt(packetsSent);
                            m_previousVideoPacketsSentLost = Integer.parseInt(packetsLost);
                        }
                    }
                    else if ("audio".equals(media))
                    {
                        if (googRtt != null)
                        {
                            m_audioSentStreamRtt[m_audioSentStreamRttIndex % MAX_VALUES] = Integer.parseInt(googRtt);
                            m_audioSentStreamRttIndex++;
                        }

                        if (packetsLost != null && packetsSent != null)
                        {
                            if ((Integer.parseInt(packetsSent) - m_previousAudioPacketsSent) == 0)
                                m_data.put(AUDIO_PACKETS_LOST_ON_SENT, 0);
                            else
                                m_data.put(AUDIO_PACKETS_LOST_ON_SENT, ((Integer.parseInt(packetsLost) - m_previousAudioPacketsSentLost) * 100) / (Integer.parseInt(packetsSent) - m_previousAudioPacketsSent));

                            m_previousAudioPacketsSent = Integer.parseInt(packetsSent);
                            m_previousAudioPacketsSentLost = Integer.parseInt(packetsLost);
                        }
                    }
                }
                else if (report.id.contains("recv"))
                {
                    String packetsReceived = reportMap.get("packetsReceived");

                    if ("video".equals(media))
                    {
                        String googJitterBufferMs = reportMap.get("googJitterBufferMs");

                        if (googJitterBufferMs != null)
                        {
                            m_videoReceiveStreamJitter[m_videoReceiveStreamJitterIndex % MAX_VALUES] = Integer.parseInt(googJitterBufferMs);
                            m_videoReceiveStreamJitterIndex++;
                        }

                        if (packetsLost != null && packetsReceived != null)
                        {
                            if ((Integer.parseInt(packetsReceived) - m_previousVideoPacketsReceived) == 0)
                                m_data.put(VIDEO_PACKETS_LOST_ON_RECV, 0);
                            else
                                m_data.put(VIDEO_PACKETS_LOST_ON_RECV, ((Integer.parseInt(packetsLost) - m_previousVideoPacketsReceivedLost) * 100) / (Integer.parseInt(packetsReceived) - m_previousVideoPacketsReceived));

                            m_previousVideoPacketsReceived = Integer.parseInt(packetsReceived);
                            m_previousVideoPacketsReceivedLost = Integer.parseInt(packetsLost);
                        }
                    }
                    else if ("audio".equals(media))
                    {
                        String googJitterReceived = reportMap.get("googJitterReceived");

                        if (googJitterReceived != null)
                        {
                            m_audioReceiveStreamJitter[m_audioReceiveStreamJitterIndex % MAX_VALUES] = Integer.parseInt(googJitterReceived);
                            m_audioReceiveStreamJitterIndex++;
                        }

                        if (packetsLost != null && packetsReceived != null)
                        {
                            if ((Integer.parseInt(packetsReceived) - m_previousAudioPacketsReceived) == 0)
                                m_data.put(AUDIO_PACKETS_LOST_ON_RECV, 0);
                            else
                                m_data.put(AUDIO_PACKETS_LOST_ON_RECV, ((Integer.parseInt(packetsLost) - m_previousAudioPacketsReceivedLost) * 100) / (Integer.parseInt(packetsReceived) - m_previousAudioPacketsReceived));

                            m_previousAudioPacketsReceived = Integer.parseInt(packetsReceived);
                            m_previousAudioPacketsReceivedLost = Integer.parseInt(packetsLost);
                        }
                    }
                }
            }
        }

        m_data.put(AUDIO_RTT_ON_SENT, average(m_audioSentStreamRtt));
        m_data.put(AUDIO_JITTER_ON_RECV, average(m_audioReceiveStreamJitter));
        m_data.put(VIDEO_RTT_ON_SENT, average(m_videoSentStreamRtt));
        m_data.put(VIDEO_JITTER_ON_RECV, average(m_videoReceiveStreamJitter));

        Quality indicator;

        if (isSpecCompliant(high()))
            indicator = Quality.HIGH_QUALITY_VALUE;
        else if (isSpecCompliant(medium()))
            indicator = Quality.MEDIUM_QUALITY_VALUE;
        else
            indicator = Quality.LOW_QUALITY_VALUE;

        return indicator;
    }

    private int average(int[] array)
    {
        int value = -1;

        if (array != null && array.length > 0)
        {
            value = 0;
            for (int arrayValue : array)
            {
                value += arrayValue;
            }
            value = value / array.length;
        }

        return value;
    }

    private Map<String, String> getReportMap(StatsReport report)
    {
        Map<String, String> reportMap = new HashMap<>();
        for (StatsReport.Value value : report.values)
        {
            reportMap.put(value.name, value.value);
        }
        return reportMap;
    }

    public enum Quality
    {
        HIGH_QUALITY_VALUE,
        MEDIUM_QUALITY_VALUE,
        LOW_QUALITY_VALUE;
    }
}
