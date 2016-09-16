FROM fcomb/jre8-sbt-alpine:v2.11_0.13
MAINTAINER Timothy Klim <snowplow@timothyklim.com>

USER root

RUN adduser -D -g '' -h /app -H snowplow
ENV WORKDIR /home/java/project

COPY . ${WORKDIR}
WORKDIR ${WORKDIR}

RUN chown -R java:java ${WORKDIR} && \
    mkdir -p /etc/snowplow && \
    mv ${WORKDIR}/examples/config.hocon.sample /etc/snowplow/collector.conf && \
    su java -c "/home/java/bin/sbt assembly" && \
    mv ${WORKDIR}/target/scala-2.11/snowplow-stream-collector / && \
    deluser --remove-home java && \
    rm -rf /var/cache/apk/* ${WORKDIR}

EXPOSE 8080

WORKDIR /

CMD /snowplow-stream-collector --config /etc/snowplow/collector.conf
