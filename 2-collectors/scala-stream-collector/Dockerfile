FROM fcomb/jre8-sbt-alpine
MAINTAINER Timothy Klim <snowplow@timothyklim.com>

USER root

RUN adduser -D -g '' -h /app -H snowplow
ENV APP /app
ENV WORKDIR /home/java/project

COPY . ${WORKDIR}
WORKDIR ${WORKDIR}

RUN chown -R java:java ${WORKDIR} && \
    mkdir -p /etc/snowplow && \
    mv ${WORKDIR}/examples/config.hocon.sample /etc/snowplow/collector.conf && \
    su java -c "/home/java/bin/sbt universal:packageZipTarball" && \
    tar -xf ${WORKDIR}/target/universal/dist.tgz -C / && \
    mv /dist ${APP} && \
    chown -R snowplow:snowplow ${APP} && \
    deluser --remove-home java && \
    rm -rf /var/cache/apk/* ${WORKDIR}

EXPOSE 8080

WORKDIR ${APP}

USER snowplow

CMD ${APP}/bin/start --config /etc/snowplow/collector.conf
