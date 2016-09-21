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
    mv ${WORKDIR}/examples/config.hocon.sample /etc/snowplow/enrich.conf && \
    mv ${WORKDIR}/config /etc/snowplow/config && \
    su java -c "/home/java/bin/sbt universal:packageZipTarball" && \
    tar -xf ${WORKDIR}/target/universal/dist.tgz -C / && \
    mv /dist ${APP} && \
    chown -R snowplow:snowplow ${APP} && \
    deluser --remove-home java && \
    rm -rf /var/cache/apk/* ${WORKDIR}

WORKDIR ${APP}

USER snowplow

ENV RESOLVER "file:/etc/snowplow/config/iglu_resolver.json"
ENV ENRICHMENTS "file:/etc/snowplow/config/enrichments"

CMD ${APP}/bin/start --config /etc/snowplow/enrich.conf --enrichments ${ENRICHMENTS} --resolver ${RESOLVER}
