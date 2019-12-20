---
---

function switchVersion(selectedOption, urlPostfix) {
  window.location.href = `{{ site.url }}{{ site.baseurl }}/docs/${selectedOption}/${urlPostfix}`;
}
