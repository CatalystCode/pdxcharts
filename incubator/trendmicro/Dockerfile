FROM ubuntu:14.04

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update \
    && apt-get install --assume-yes apt-utils wget \
    && sed -i.bak 's/101/0/' /usr/sbin/policy-rc.d
ADD run.sh /usr/local/bin/run.sh
RUN chmod +x /usr/local/bin/run.sh
CMD /usr/local/bin/run.sh
CMD exec /bin/bash -c "trap : TERM INT; sleep infinity & wait"
