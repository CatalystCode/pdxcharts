FROM centos:7

ENV NEWRELIC_DOWNLOAD_URL https://download.newrelic.com/infrastructure_agent/linux/yum/el/7/x86_64/newrelic-infra.repo
ENV NEWRELIC_GPG_KEY_URL https://download.newrelic.com/infrastructure_agent/gpg/newrelic-infra.gpg
ENV NRIA_LOGLEVEL="info"
ENV NRIA_VERBOSE=0
ENV NRIA_OVERRIDE_HOST_ROOT="/mnt/ROOT"

# HACK: the New Relic Infrastructure Agent will panic if the config file
# isn't present. It will also fail to start if the file is present but
# contains no information. Since the agent is *also* configured via
# environment variables, we create a dummy newrelic-infra.yml, which
# allows the agent to start using the license key provided by
# the provided environment variable NRIA_LICENSE_KEY.
RUN echo "null: null" > /etc/newrelic-infra.yml

RUN rpm --import $NEWRELIC_GPG_KEY_URL \
  && curl -L $NEWRELIC_DOWNLOAD_URL -o /etc/yum.repos.d/newrelic-infra.repo \
  && yum -q makecache -y --disablerepo='*' --enablerepo='newrelic-infra' \
  && yum -y install newrelic-infra dmidecode policycoreutils ca-certificates \
  && yum clean all

CMD /usr/bin/newrelic-infra